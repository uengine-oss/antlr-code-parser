"""대출/반납 서비스 - swing_system의 svc_usage.c + svc_billing.c에 대응"""

from datetime import date, timedelta

from ..core.database import Database
from ..core.config import Config
from ..core.exceptions import (
    BookNotFoundError,
    BookUnavailableError,
    LoanLimitExceededError,
    LoanNotFoundError,
    MemberNotFoundError,
)
from ..core.logger import get_logger
from ..models.book import BookStatus
from ..models.loan import Loan, LoanStatus

logger = get_logger(__name__)


class LoanService:
    def __init__(self, db: Database, config: Config):
        self._db = db
        self._config = config

    def borrow(self, book_id: int, member_id: int) -> Loan:
        # 회원 존재 확인
        member = self._db.fetchone("SELECT * FROM members WHERE id = ? AND is_active = 1", (member_id,))
        if member is None:
            raise MemberNotFoundError(member_id)

        # 도서 존재 및 상태 확인
        book = self._db.fetchone("SELECT * FROM books WHERE id = ?", (book_id,))
        if book is None:
            raise BookNotFoundError(book_id)
        if book["status"] != BookStatus.AVAILABLE.value:
            raise BookUnavailableError(book_id)

        # 대출 한도 확인
        active_count = self._db.fetchone(
            "SELECT COUNT(*) as cnt FROM loans WHERE member_id = ? AND status = 'ACTIVE'",
            (member_id,),
        )["cnt"]
        if active_count >= self._config.max_loans_per_member:
            raise LoanLimitExceededError(member_id, self._config.max_loans_per_member)

        # 대출 처리
        loan_date = date.today()
        due_date = loan_date + timedelta(days=self._config.loan_duration_days)

        cursor = self._db.execute(
            "INSERT INTO loans (book_id, member_id, loan_date, due_date) VALUES (?, ?, ?, ?)",
            (book_id, member_id, loan_date.isoformat(), due_date.isoformat()),
        )
        self._db.execute(
            "UPDATE books SET status = ? WHERE id = ?",
            (BookStatus.LOANED.value, book_id),
        )
        self._db.commit()

        loan_id = cursor.lastrowid
        logger.info("대출: 도서#%d -> 회원#%d (반납기한: %s)", book_id, member_id, due_date)

        row = self._db.fetchone("SELECT * FROM loans WHERE id = ?", (loan_id,))
        return Loan.from_row(row)

    def return_book(self, loan_id: int) -> Loan:
        row = self._db.fetchone("SELECT * FROM loans WHERE id = ?", (loan_id,))
        if row is None:
            raise LoanNotFoundError(loan_id)

        loan = Loan.from_row(row)
        if loan.status != LoanStatus.ACTIVE:
            raise LoanNotFoundError(loan_id)

        today = date.today()
        fine = 0
        due = date.fromisoformat(loan.due_date)
        if today > due:
            overdue_days = (today - due).days
            fine = overdue_days * self._config.overdue_fine_per_day
            logger.info("연체 반납: 대출#%d, %d일 연체, 연체료 %d원", loan_id, overdue_days, fine)

        self._db.execute(
            "UPDATE loans SET status = ?, return_date = ?, fine = ? WHERE id = ?",
            (LoanStatus.RETURNED.value, today.isoformat(), fine, loan_id),
        )
        self._db.execute(
            "UPDATE books SET status = ? WHERE id = ?",
            (BookStatus.AVAILABLE.value, loan.book_id),
        )
        self._db.commit()

        logger.info("반납 완료: 대출#%d, 도서#%d", loan_id, loan.book_id)
        row = self._db.fetchone("SELECT * FROM loans WHERE id = ?", (loan_id,))
        return Loan.from_row(row)

    def get_active_loans(self, member_id: int) -> list[Loan]:
        rows = self._db.fetchall(
            "SELECT * FROM loans WHERE member_id = ? AND status = 'ACTIVE' ORDER BY due_date",
            (member_id,),
        )
        return [Loan.from_row(r) for r in rows]

    def get_overdue_loans(self) -> list[Loan]:
        today = date.today().isoformat()
        rows = self._db.fetchall(
            "SELECT * FROM loans WHERE status = 'ACTIVE' AND due_date < ? ORDER BY due_date",
            (today,),
        )
        return [Loan.from_row(r) for r in rows]

    def get_loan_history(self, member_id: int) -> list[Loan]:
        rows = self._db.fetchall(
            "SELECT * FROM loans WHERE member_id = ? ORDER BY loan_date DESC",
            (member_id,),
        )
        return [Loan.from_row(r) for r in rows]

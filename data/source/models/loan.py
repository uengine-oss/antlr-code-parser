"""대출 모델"""

from dataclasses import dataclass
from enum import Enum


class LoanStatus(Enum):
    ACTIVE = "ACTIVE"
    RETURNED = "RETURNED"
    OVERDUE = "OVERDUE"


@dataclass
class Loan:
    id: int
    book_id: int
    member_id: int
    loan_date: str
    due_date: str
    return_date: str | None = None
    status: LoanStatus = LoanStatus.ACTIVE
    fine: int = 0

    @classmethod
    def from_row(cls, row) -> "Loan":
        return cls(
            id=row["id"],
            book_id=row["book_id"],
            member_id=row["member_id"],
            loan_date=row["loan_date"],
            due_date=row["due_date"],
            return_date=row["return_date"],
            status=LoanStatus(row["status"]),
            fine=row["fine"],
        )

    def display(self) -> str:
        status_kr = {
            LoanStatus.ACTIVE: "대출중",
            LoanStatus.RETURNED: "반납완료",
            LoanStatus.OVERDUE: "연체",
        }
        info = (
            f"[대출#{self.id}] 도서#{self.book_id} -> 회원#{self.member_id} "
            f"({self.loan_date} ~ {self.due_date}) "
            f"{status_kr.get(self.status, '알수없음')}"
        )
        if self.fine > 0:
            info += f" 연체료: {self.fine:,}원"
        return info

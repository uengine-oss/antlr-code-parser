"""통계 서비스 - swing_system의 tp_print_stats에 대응"""

from ..core.database import Database
from ..core.logger import get_logger

logger = get_logger(__name__)


class StatsService:
    def __init__(self, db: Database):
        self._db = db

    def get_summary(self) -> dict:
        total_books = self._db.fetchone("SELECT COUNT(*) as cnt FROM books")["cnt"]
        available_books = self._db.fetchone(
            "SELECT COUNT(*) as cnt FROM books WHERE status = 'AVAILABLE'"
        )["cnt"]
        loaned_books = self._db.fetchone(
            "SELECT COUNT(*) as cnt FROM books WHERE status = 'LOANED'"
        )["cnt"]
        total_members = self._db.fetchone(
            "SELECT COUNT(*) as cnt FROM members WHERE is_active = 1"
        )["cnt"]
        active_loans = self._db.fetchone(
            "SELECT COUNT(*) as cnt FROM loans WHERE status = 'ACTIVE'"
        )["cnt"]
        total_loans = self._db.fetchone("SELECT COUNT(*) as cnt FROM loans")["cnt"]
        total_fines = self._db.fetchone(
            "SELECT COALESCE(SUM(fine), 0) as total FROM loans"
        )["total"]

        return {
            "total_books": total_books,
            "available_books": available_books,
            "loaned_books": loaned_books,
            "total_members": total_members,
            "active_loans": active_loans,
            "total_loans": total_loans,
            "total_fines": total_fines,
        }

    def print_summary(self) -> None:
        s = self.get_summary()
        print()
        print("=" * 40)
        print("       도서관 시스템 통계")
        print("=" * 40)
        print(f"  총 도서 수     : {s['total_books']:,}권")
        print(f"    대출 가능    : {s['available_books']:,}권")
        print(f"    대출 중      : {s['loaned_books']:,}권")
        print(f"  총 회원 수     : {s['total_members']:,}명")
        print(f"  현재 대출 건수 : {s['active_loans']:,}건")
        print(f"  누적 대출 건수 : {s['total_loans']:,}건")
        print(f"  누적 연체료    : {s['total_fines']:,}원")
        print("=" * 40)

    def get_popular_books(self, limit: int = 5) -> list[dict]:
        rows = self._db.fetchall(
            "SELECT b.id, b.title, b.author, COUNT(l.id) as loan_count "
            "FROM books b JOIN loans l ON b.id = l.book_id "
            "GROUP BY b.id ORDER BY loan_count DESC LIMIT ?",
            (limit,),
        )
        return [
            {"id": r["id"], "title": r["title"], "author": r["author"], "loan_count": r["loan_count"]}
            for r in rows
        ]

    def get_active_members(self, limit: int = 5) -> list[dict]:
        rows = self._db.fetchall(
            "SELECT m.id, m.name, m.phone, COUNT(l.id) as loan_count "
            "FROM members m JOIN loans l ON m.id = l.member_id "
            "GROUP BY m.id ORDER BY loan_count DESC LIMIT ?",
            (limit,),
        )
        return [
            {"id": r["id"], "name": r["name"], "phone": r["phone"], "loan_count": r["loan_count"]}
            for r in rows
        ]

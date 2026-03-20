"""도서 모델"""

from dataclasses import dataclass
from enum import Enum


class BookStatus(Enum):
    AVAILABLE = "AVAILABLE"
    LOANED = "LOANED"
    LOST = "LOST"
    DAMAGED = "DAMAGED"


@dataclass
class Book:
    id: int
    isbn: str
    title: str
    author: str
    publisher: str = ""
    year: int = 0
    category: str = "일반"
    status: BookStatus = BookStatus.AVAILABLE
    created_at: str = ""

    @classmethod
    def from_row(cls, row) -> "Book":
        return cls(
            id=row["id"],
            isbn=row["isbn"],
            title=row["title"],
            author=row["author"],
            publisher=row["publisher"],
            year=row["year"],
            category=row["category"],
            status=BookStatus(row["status"]),
            created_at=row["created_at"],
        )

    def display(self) -> str:
        status_kr = {
            BookStatus.AVAILABLE: "대출가능",
            BookStatus.LOANED: "대출중",
            BookStatus.LOST: "분실",
            BookStatus.DAMAGED: "훼손",
        }
        return (
            f"[{self.id}] {self.title} / {self.author} "
            f"({self.publisher}, {self.year}) "
            f"[{self.category}] - {status_kr.get(self.status, '알수없음')}"
        )

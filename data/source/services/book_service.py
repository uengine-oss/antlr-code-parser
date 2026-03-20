"""도서 관리 서비스 - swing_system의 svc_plan.c에 대응"""

from ..core.database import Database
from ..core.exceptions import BookNotFoundError
from ..core.logger import get_logger
from ..models.book import Book, BookStatus

logger = get_logger(__name__)


class BookService:
    def __init__(self, db: Database):
        self._db = db

    def register(self, isbn: str, title: str, author: str,
                 publisher: str = "", year: int = 0,
                 category: str = "일반") -> Book:
        cursor = self._db.execute(
            "INSERT INTO books (isbn, title, author, publisher, year, category) "
            "VALUES (?, ?, ?, ?, ?, ?)",
            (isbn, title, author, publisher, year, category),
        )
        self._db.commit()
        book_id = cursor.lastrowid
        logger.info("도서 등록: [%d] %s / %s", book_id, title, author)
        row = self._db.fetchone("SELECT * FROM books WHERE id = ?", (book_id,))
        return Book.from_row(row)

    def find_by_id(self, book_id: int) -> Book:
        row = self._db.fetchone("SELECT * FROM books WHERE id = ?", (book_id,))
        if row is None:
            raise BookNotFoundError(book_id)
        return Book.from_row(row)

    def search(self, keyword: str) -> list[Book]:
        rows = self._db.fetchall(
            "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ?",
            (f"%{keyword}%", f"%{keyword}%", f"%{keyword}%"),
        )
        return [Book.from_row(r) for r in rows]

    def list_all(self) -> list[Book]:
        rows = self._db.fetchall("SELECT * FROM books ORDER BY id")
        return [Book.from_row(r) for r in rows]

    def update_status(self, book_id: int, status: BookStatus) -> None:
        book = self.find_by_id(book_id)
        self._db.execute(
            "UPDATE books SET status = ? WHERE id = ?",
            (status.value, book_id),
        )
        self._db.commit()
        logger.info("도서 상태 변경: [%d] %s -> %s", book_id, book.status.value, status.value)

    def delete(self, book_id: int) -> None:
        self.find_by_id(book_id)
        self._db.execute("DELETE FROM books WHERE id = ?", (book_id,))
        self._db.commit()
        logger.info("도서 삭제: [%d]", book_id)

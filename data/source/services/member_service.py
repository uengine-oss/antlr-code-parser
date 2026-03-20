"""회원 관리 서비스 - swing_system의 svc_subscriber.c에 대응"""

from ..core.database import Database
from ..core.exceptions import MemberNotFoundError
from ..core.logger import get_logger
from ..models.member import Member, MemberGrade

logger = get_logger(__name__)


class MemberService:
    def __init__(self, db: Database):
        self._db = db

    def register(self, name: str, phone: str, email: str = "") -> Member:
        cursor = self._db.execute(
            "INSERT INTO members (name, phone, email) VALUES (?, ?, ?)",
            (name, phone, email),
        )
        self._db.commit()
        member_id = cursor.lastrowid
        logger.info("회원 등록: [%d] %s (%s)", member_id, name, phone)
        row = self._db.fetchone("SELECT * FROM members WHERE id = ?", (member_id,))
        return Member.from_row(row)

    def find_by_id(self, member_id: int) -> Member:
        row = self._db.fetchone("SELECT * FROM members WHERE id = ?", (member_id,))
        if row is None:
            raise MemberNotFoundError(member_id)
        return Member.from_row(row)

    def find_by_phone(self, phone: str) -> Member | None:
        row = self._db.fetchone("SELECT * FROM members WHERE phone = ?", (phone,))
        if row is None:
            return None
        return Member.from_row(row)

    def search(self, keyword: str) -> list[Member]:
        rows = self._db.fetchall(
            "SELECT * FROM members WHERE name LIKE ? OR phone LIKE ?",
            (f"%{keyword}%", f"%{keyword}%"),
        )
        return [Member.from_row(r) for r in rows]

    def list_all(self) -> list[Member]:
        rows = self._db.fetchall("SELECT * FROM members WHERE is_active = 1 ORDER BY id")
        return [Member.from_row(r) for r in rows]

    def upgrade_grade(self, member_id: int, grade: MemberGrade) -> None:
        self.find_by_id(member_id)
        self._db.execute(
            "UPDATE members SET grade = ? WHERE id = ?",
            (grade.value, member_id),
        )
        self._db.commit()
        logger.info("회원 등급 변경: [%d] -> %s", member_id, grade.value)

    def deactivate(self, member_id: int) -> None:
        self.find_by_id(member_id)
        self._db.execute("UPDATE members SET is_active = 0 WHERE id = ?", (member_id,))
        self._db.commit()
        logger.info("회원 비활성화: [%d]", member_id)

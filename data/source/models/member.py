"""회원 모델"""

from dataclasses import dataclass
from enum import Enum


class MemberGrade(Enum):
    NORMAL = "NORMAL"
    SILVER = "SILVER"
    GOLD = "GOLD"
    VIP = "VIP"


@dataclass
class Member:
    id: int
    name: str
    phone: str
    email: str = ""
    grade: MemberGrade = MemberGrade.NORMAL
    join_date: str = ""
    is_active: bool = True

    @classmethod
    def from_row(cls, row) -> "Member":
        return cls(
            id=row["id"],
            name=row["name"],
            phone=row["phone"],
            email=row["email"],
            grade=MemberGrade(row["grade"]),
            join_date=row["join_date"],
            is_active=bool(row["is_active"]),
        )

    def display(self) -> str:
        grade_kr = {
            MemberGrade.NORMAL: "일반",
            MemberGrade.SILVER: "실버",
            MemberGrade.GOLD: "골드",
            MemberGrade.VIP: "VIP",
        }
        status = "활성" if self.is_active else "비활성"
        return (
            f"[{self.id}] {self.name} ({self.phone}) "
            f"등급: {grade_kr.get(self.grade, '일반')} / {status}"
        )

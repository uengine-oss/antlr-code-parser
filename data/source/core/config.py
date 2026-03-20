"""설정 파일 파서 - swing_system의 config.c에 대응"""

from dataclasses import dataclass, field


@dataclass
class Config:
    db_path: str = "./data/library.db"
    log_dir: str = "./logs"
    log_level: str = "INFO"
    max_loans_per_member: int = 5
    loan_duration_days: int = 14
    overdue_fine_per_day: int = 500
    host: str = "127.0.0.1"
    port: int = 8080

    @classmethod
    def from_file(cls, filepath: str) -> "Config":
        config = cls()
        try:
            with open(filepath, "r", encoding="utf-8") as f:
                for line in f:
                    line = line.strip()
                    if not line or line.startswith("#"):
                        continue
                    if "=" not in line:
                        continue
                    key, _, value = line.partition("=")
                    key = key.strip()
                    value = value.strip()
                    if hasattr(config, key):
                        field_type = type(getattr(config, key))
                        setattr(config, key, field_type(value))
        except FileNotFoundError:
            print(f"[CONFIG] 설정 파일을 찾을 수 없습니다: {filepath}, 기본값 사용")
        return config

    def print_config(self) -> None:
        print("=" * 44)
        print("     도서관 관리 시스템 설정")
        print("=" * 44)
        print(f"  DB 경로        : {self.db_path}")
        print(f"  로그 디렉토리  : {self.log_dir}")
        print(f"  로그 레벨      : {self.log_level}")
        print(f"  최대 대출 권수 : {self.max_loans_per_member}권")
        print(f"  대출 기간      : {self.loan_duration_days}일")
        print(f"  연체료 (일)    : {self.overdue_fine_per_day}원")
        print(f"  서버           : {self.host}:{self.port}")
        print("=" * 44)

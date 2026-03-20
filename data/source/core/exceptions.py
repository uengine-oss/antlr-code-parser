"""도서관 시스템 커스텀 예외 정의"""


class LibraryError(Exception):
    """기본 라이브러리 에러"""
    pass


class BookNotFoundError(LibraryError):
    """도서를 찾을 수 없음"""
    def __init__(self, book_id: int):
        super().__init__(f"도서를 찾을 수 없습니다: ID={book_id}")
        self.book_id = book_id


class MemberNotFoundError(LibraryError):
    """회원을 찾을 수 없음"""
    def __init__(self, member_id: int):
        super().__init__(f"회원을 찾을 수 없습니다: ID={member_id}")
        self.member_id = member_id


class LoanLimitExceededError(LibraryError):
    """대출 한도 초과"""
    def __init__(self, member_id: int, limit: int):
        super().__init__(f"대출 한도 초과: 회원 ID={member_id}, 한도={limit}권")
        self.member_id = member_id
        self.limit = limit


class BookUnavailableError(LibraryError):
    """도서 대출 불가 (이미 대출 중)"""
    def __init__(self, book_id: int):
        super().__init__(f"대출 불가능한 도서입니다: ID={book_id}")
        self.book_id = book_id


class LoanNotFoundError(LibraryError):
    """대출 기록을 찾을 수 없음"""
    def __init__(self, loan_id: int):
        super().__init__(f"대출 기록을 찾을 수 없습니다: ID={loan_id}")
        self.loan_id = loan_id

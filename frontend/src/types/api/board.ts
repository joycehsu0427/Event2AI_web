export interface Board {
  id: string
  title: string
  description: string
  color: string
  ownerUserId: string
  ownerName: string
  currentUserRole?: BoardMemberRole
}

export type BoardMemberRole = 'VIEWER' | 'EDITOR' | 'OWNER'

export interface BoardMember {
  boardId: string
  userId: string
  username: string
  email: string
  role: BoardMemberRole
}


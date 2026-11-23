export type Role = 'RIDER' | 'DRIVER' | 'ADMIN';
export type UserStatus = 'ACTIVE' | 'BLOCKED' | 'PENDING';
export type OtpPurpose = 'LOGIN' | 'SIGNUP' | 'RESET_PASSWORD';

export interface User {
  id: string;
  phone: string;
  email?: string;
  fullName: string;
  role: Role;
  status: UserStatus;
  phoneVerified: boolean;
  createdAt: string;
}

export interface TokenPair {
  accessToken: string;
  refreshToken: string;
  expiresInSec: number;
}

export interface AuthResponse extends TokenPair {
  user: User;
}

export interface MessageResponse {
  message: string;
}

export interface RegisterBody {
  fullName: string;
  phone: string;
  email?: string;
  role: Role;
  password?: string;
}

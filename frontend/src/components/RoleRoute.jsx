import { Navigate, Outlet } from 'react-router-dom';
import { getSession } from '../services/authService';
export default function RoleRoute({ roles }) {
  const session = getSession();
  if (!session?.token) return <Navigate to="/login" replace />;
  return roles.includes(session.role) ? <Outlet /> : <p role="alert">Votre rôle ne permet pas d’accéder à cette page.</p>;
}

import { Navigate, Outlet } from 'react-router-dom';
import { getSession } from '../services/authService';
export default function ProtectedRoute() {
  return getSession()?.token ? <Outlet /> : <Navigate to="/login" replace />;
}

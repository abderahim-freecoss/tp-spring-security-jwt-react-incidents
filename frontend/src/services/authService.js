import api from './api';
export const sessionKey = 'incident-session';
export function getSession() {
  try { return JSON.parse(sessionStorage.getItem(sessionKey)); }
  catch { sessionStorage.removeItem(sessionKey); return null; }
}
export function logout() { sessionStorage.removeItem(sessionKey); }
export async function login(input) {
  const { data } = await api.post('/auth/login', input);
  sessionStorage.setItem(sessionKey, JSON.stringify(data));
  return data;
}
export async function register(input) { return (await api.post('/auth/register', input)).data; }

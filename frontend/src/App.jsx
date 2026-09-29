import { Route, Routes } from 'react-router-dom';
import LandingPage from './pages/LandingPage.jsx';
import SigninPage from './pages/SigninPage.jsx';
import SignupPage from './pages/SignupPage.jsx';
import DashboardPage from './pages/DashboardPage.jsx';
import ProjectsPage from './pages/ProjectsPage.jsx';
import ProjectBoardPage from './pages/ProjectBoardPage.jsx';
import MyTasksPage from './pages/MyTasksPage.jsx';
import PlanningPage from './pages/PlanningPage.jsx';
import RemindersPage from './pages/RemindersPage.jsx';
import AdminUsersPage from './pages/AdminUsersPage.jsx';
import NotFoundPage from './pages/NotFoundPage.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import RoleRoute from './components/RoleRoute.jsx';
import AppShell from './components/AppShell.jsx';

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />
      <Route path="/signin" element={<SigninPage />} />
      <Route path="/signup" element={<SignupPage />} />

      <Route element={<ProtectedRoute />}>
        <Route path="/app" element={<AppShell />}>
          <Route index element={<DashboardPage />} />
          <Route path="projects" element={<ProjectsPage />} />
          <Route path="projects/:id" element={<ProjectBoardPage />} />
          <Route path="my-tasks" element={<MyTasksPage />} />
          <Route path="planning" element={<PlanningPage />} />
          <Route path="reminders" element={<RemindersPage />} />

          <Route element={<RoleRoute roles={['ADMIN']} />}>
            <Route path="admin/users" element={<AdminUsersPage />} />
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}

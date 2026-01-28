import { ReactNode } from 'react';
import { LayoutDashboard, Film, Calendar, Users, BarChart3, Building2, ArrowLeft } from 'lucide-react';

interface ManagerLayoutProps {
  children: ReactNode;
  currentPage: string;
  onNavigate: (page: string) => void;
  onBackToUser: () => void;
}

export function ManagerLayout({ children, currentPage, onNavigate, onBackToUser }: ManagerLayoutProps) {
  const menuItems = [
    { id: 'dashboard', label: 'Tổng quan', icon: LayoutDashboard },
    { id: 'showtimes', label: 'Lịch chiếu', icon: Calendar },
    { id: 'bookings', label: 'Đặt vé', icon: Users },
    { id: 'reports', label: 'Báo cáo', icon: BarChart3 },
  ];

  return (
    <div className="min-h-screen bg-background flex">
      {/* Sidebar */}
      <aside className="w-64 bg-card border-r border-border flex flex-col">
        <div className="p-6 border-b border-border">
          <div className="flex items-center gap-2 mb-2">
            <Building2 className="w-8 h-8 text-blue-500" />
            <div>
              <h2 className="font-semibold">Quản lý rạp</h2>
              <p className="text-xs text-muted-foreground">Cinema Manager</p>
            </div>
          </div>
        </div>

        <nav className="flex-1 p-4">
          <div className="space-y-2">
            {menuItems.map((item) => {
              const Icon = item.icon;
              return (
                <button
                  key={item.id}
                  onClick={() => onNavigate(item.id)}
                  className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
                    currentPage === item.id
                      ? 'bg-blue-600 text-white'
                      : 'hover:bg-accent'
                  }`}
                >
                  <Icon className="w-5 h-5" />
                  <span>{item.label}</span>
                </button>
              );
            })}
          </div>
        </nav>

        <div className="p-4 border-t border-border">
          <button
            onClick={onBackToUser}
            className="w-full flex items-center gap-2 px-4 py-3 rounded-lg hover:bg-accent transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Về trang chủ</span>
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 overflow-auto">
        <div className="container mx-auto p-8">
          {children}
        </div>
      </main>
    </div>
  );
}

import { ReactNode } from 'react';
import {
  LayoutDashboard,
  Film,
  Building2,
  Calendar,
  Ticket,
  BarChart3,
  ArrowLeft,
  Menu,
} from 'lucide-react';
import { Button } from '@/app/components/ui/button';
import { ScrollArea } from '@/app/components/ui/scroll-area';

interface AdminLayoutProps {
  children: ReactNode;
  currentPage: string;
  onNavigate: (page: string) => void;
  onBackToUser: () => void;
}

export function AdminLayout({ children, currentPage, onNavigate, onBackToUser }: AdminLayoutProps) {
  const menuItems = [
    { id: 'dashboard', label: 'Tổng quan', icon: LayoutDashboard },
    { id: 'movies', label: 'Quản lý phim', icon: Film },
    { id: 'cinemas', label: 'Quản lý rạp', icon: Building2 },
    { id: 'showtimes', label: 'Quản lý suất chiếu', icon: Calendar },
    { id: 'bookings', label: 'Quản lý đặt vé', icon: Ticket },
    { id: 'reports', label: 'Báo cáo & Thống kê', icon: BarChart3 },
  ];

  return (
    <div className="min-h-screen bg-background">
      <div className="flex h-screen overflow-hidden">
        {/* Sidebar */}
        <aside className="hidden md:flex flex-col w-64 border-r border-border bg-card">
          <div className="p-6 border-b border-border">
            <div className="flex items-center gap-2 mb-4">
              <Film className="w-8 h-8 text-primary" />
              <span className="text-xl font-semibold">Admin Panel</span>
            </div>
            <Button variant="outline" onClick={onBackToUser} className="w-full gap-2">
              <ArrowLeft className="w-4 h-4" />
              Về trang người dùng
            </Button>
          </div>

          <ScrollArea className="flex-1">
            <nav className="p-4 space-y-2">
              {menuItems.map((item) => {
                const Icon = item.icon;
                return (
                  <button
                    key={item.id}
                    onClick={() => onNavigate(item.id)}
                    className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
                      currentPage === item.id
                        ? 'bg-primary text-primary-foreground'
                        : 'hover:bg-accent'
                    }`}
                  >
                    <Icon className="w-5 h-5" />
                    <span>{item.label}</span>
                  </button>
                );
              })}
            </nav>
          </ScrollArea>
        </aside>

        {/* Main Content */}
        <main className="flex-1 overflow-auto">
          <div className="p-8">{children}</div>
        </main>
      </div>

      {/* Mobile Navigation */}
      <nav className="md:hidden fixed bottom-0 left-0 right-0 bg-card border-t border-border z-50">
        <div className="flex items-center justify-around py-2">
          {menuItems.slice(0, 4).map((item) => {
            const Icon = item.icon;
            return (
              <button
                key={item.id}
                onClick={() => onNavigate(item.id)}
                className={`flex flex-col items-center gap-1 px-3 py-2 rounded-lg transition-colors ${
                  currentPage === item.id ? 'text-primary' : 'text-muted-foreground'
                }`}
              >
                <Icon className="w-5 h-5" />
                <span className="text-xs">{item.label.split(' ')[1] || item.label}</span>
              </button>
            );
          })}
        </div>
      </nav>
    </div>
  );
}

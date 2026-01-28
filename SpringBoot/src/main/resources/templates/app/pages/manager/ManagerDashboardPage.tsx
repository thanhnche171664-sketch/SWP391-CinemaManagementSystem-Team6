import { Card } from '@/app/components/ui/card';
import { BarChart3, Calendar, Users, TrendingUp } from 'lucide-react';

export function ManagerDashboardPage() {
  const stats = [
    {
      title: 'Doanh thu hôm nay',
      value: '45.500.000đ',
      icon: TrendingUp,
      trend: '+12%',
      color: 'text-green-500',
    },
    {
      title: 'Vé đã bán',
      value: '245',
      icon: Users,
      trend: '+8%',
      color: 'text-blue-500',
    },
    {
      title: 'Suất chiếu hôm nay',
      value: '24',
      icon: Calendar,
      trend: '0%',
      color: 'text-purple-500',
    },
    {
      title: 'Tỷ lệ lấp đầy',
      value: '68%',
      icon: BarChart3,
      trend: '+5%',
      color: 'text-orange-500',
    },
  ];

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold">Tổng quan rạp chiếu</h1>
        <p className="text-muted-foreground mt-2">
          Theo dõi hoạt động và hiệu suất rạp chiếu của bạn
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {stats.map((stat) => {
          const Icon = stat.icon;
          return (
            <Card key={stat.title} className="p-6">
              <div className="flex items-center justify-between mb-4">
                <div className={`p-3 rounded-lg bg-accent ${stat.color}`}>
                  <Icon className="w-6 h-6" />
                </div>
                <span className={`text-sm font-medium ${stat.color}`}>
                  {stat.trend}
                </span>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">{stat.title}</p>
                <p className="text-2xl font-bold mt-1">{stat.value}</p>
              </div>
            </Card>
          );
        })}
      </div>

      {/* Recent Activities */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card className="p-6">
          <h3 className="text-lg font-semibold mb-4">Phim phổ biến nhất</h3>
          <div className="space-y-4">
            {[
              { title: 'Oppenheimer', bookings: 85, revenue: '12.500.000đ' },
              { title: 'Barbie', bookings: 72, revenue: '10.800.000đ' },
              { title: 'Dune: Part Two', bookings: 58, revenue: '9.200.000đ' },
            ].map((movie) => (
              <div
                key={movie.title}
                className="flex items-center justify-between p-3 rounded-lg bg-accent/50"
              >
                <div>
                  <p className="font-medium">{movie.title}</p>
                  <p className="text-sm text-muted-foreground">
                    {movie.bookings} vé đã bán
                  </p>
                </div>
                <p className="font-semibold">{movie.revenue}</p>
              </div>
            ))}
          </div>
        </Card>

        <Card className="p-6">
          <h3 className="text-lg font-semibold mb-4">Suất chiếu sắp tới</h3>
          <div className="space-y-4">
            {[
              { time: '14:00', movie: 'Oppenheimer', room: 'Phòng 1', seats: '32/100' },
              { time: '16:30', movie: 'Barbie', room: 'Phòng 2', seats: '45/80' },
              { time: '19:00', movie: 'Dune: Part Two', room: 'Phòng VIP', seats: '28/60' },
            ].map((showtime, index) => (
              <div
                key={index}
                className="flex items-center justify-between p-3 rounded-lg bg-accent/50"
              >
                <div className="flex items-center gap-4">
                  <div className="text-center">
                    <p className="font-semibold text-lg">{showtime.time}</p>
                    <p className="text-xs text-muted-foreground">{showtime.room}</p>
                  </div>
                  <div>
                    <p className="font-medium">{showtime.movie}</p>
                    <p className="text-sm text-muted-foreground">
                      Đã bán: {showtime.seats}
                    </p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </Card>
      </div>
    </div>
  );
}

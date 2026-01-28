import { mockDashboardStats } from '@/data/mockData';
import { Card } from '@/app/components/ui/card';
import { TrendingUp, TrendingDown, DollarSign, Ticket, Film, ShoppingCart } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, LineChart, Line, PieChart, Pie, Cell } from 'recharts';

const revenueData = [
  { month: 'T1', revenue: 180000000 },
  { month: 'T2', revenue: 220000000 },
  { month: 'T3', revenue: 195000000 },
  { month: 'T4', revenue: 210000000 },
  { month: 'T5', revenue: 245000000 },
  { month: 'T6', revenue: 230000000 },
];

const movieData = [
  { name: 'Oppenheimer', tickets: 3240 },
  { name: 'Barbie', tickets: 2890 },
  { name: 'Dune 2', tickets: 2650 },
  { name: 'The Batman', tickets: 2340 },
  { name: 'Others', tickets: 7420 },
];

const COLORS = ['hsl(var(--chart-1))', 'hsl(var(--chart-2))', 'hsl(var(--chart-3))', 'hsl(var(--chart-4))', 'hsl(var(--chart-5))'];

export function DashboardPage() {
  const stats = mockDashboardStats;

  const StatCard = ({
    title,
    value,
    change,
    icon: Icon,
    format = 'number',
  }: {
    title: string;
    value: number;
    change: number;
    icon: any;
    format?: 'number' | 'currency';
  }) => {
    const isPositive = change >= 0;
    const formattedValue =
      format === 'currency'
        ? `${(value / 1000000000).toFixed(2)}B`
        : value.toLocaleString('vi-VN');

    return (
      <Card className="p-6">
        <div className="flex items-start justify-between">
          <div className="flex-1">
            <p className="text-sm text-muted-foreground mb-1">{title}</p>
            <h3 className="text-3xl font-bold mb-2">{formattedValue}</h3>
            <div className={`flex items-center gap-1 text-sm ${isPositive ? 'text-green-500' : 'text-red-500'}`}>
              {isPositive ? <TrendingUp className="w-4 h-4" /> : <TrendingDown className="w-4 h-4" />}
              <span>{Math.abs(change)}%</span>
              <span className="text-muted-foreground">so với tháng trước</span>
            </div>
          </div>
          <div className="p-3 rounded-lg bg-primary/10">
            <Icon className="w-6 h-6 text-primary" />
          </div>
        </div>
      </Card>
    );
  };

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold mb-2">Tổng quan</h1>
        <p className="text-muted-foreground">Dashboard quản lý hệ thống rạp chiếu phim</p>
      </div>

      {/* Stats Grid */}
      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
        <StatCard
          title="Tổng doanh thu"
          value={stats.totalRevenue}
          change={stats.revenueGrowth}
          icon={DollarSign}
          format="currency"
        />
        <StatCard
          title="Vé đã bán"
          value={stats.ticketsSold}
          change={stats.ticketsGrowth}
          icon={Ticket}
        />
        <StatCard
          title="Tổng số phim"
          value={stats.totalMovies}
          change={5.2}
          icon={Film}
        />
        <StatCard
          title="Tổng đơn đặt"
          value={stats.totalBookings}
          change={7.8}
          icon={ShoppingCart}
        />
      </div>

      {/* Charts */}
      <div className="grid gap-6 lg:grid-cols-2">
        {/* Revenue Chart */}
        <Card className="p-6">
          <h3 className="font-semibold mb-4">Doanh thu 6 tháng gần đây</h3>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={revenueData}>
              <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
              <XAxis dataKey="month" stroke="hsl(var(--muted-foreground))" />
              <YAxis stroke="hsl(var(--muted-foreground))" />
              <Tooltip
                contentStyle={{
                  backgroundColor: 'hsl(var(--card))',
                  border: '1px solid hsl(var(--border))',
                  borderRadius: '8px',
                }}
                formatter={(value: number) => [
                  `${(value / 1000000).toFixed(0)}M đ`,
                  'Doanh thu',
                ]}
              />
              <Bar dataKey="revenue" fill="hsl(var(--primary))" radius={[8, 8, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </Card>

        {/* Movie Performance */}
        <Card className="p-6">
          <h3 className="font-semibold mb-4">Phim bán chạy nhất</h3>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={movieData}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={(entry) => entry.name}
                outerRadius={100}
                fill="#8884d8"
                dataKey="tickets"
              >
                {movieData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip
                contentStyle={{
                  backgroundColor: 'hsl(var(--card))',
                  border: '1px solid hsl(var(--border))',
                  borderRadius: '8px',
                }}
              />
            </PieChart>
          </ResponsiveContainer>
        </Card>
      </div>

      {/* Recent Activity */}
      <Card className="p-6">
        <h3 className="font-semibold mb-4">Hoạt động gần đây</h3>
        <div className="space-y-4">
          {[
            { action: 'Đặt vé mới', movie: 'Oppenheimer', user: 'Nguyễn Văn A', time: '5 phút trước' },
            { action: 'Đặt vé mới', movie: 'Barbie', user: 'Trần Thị B', time: '15 phút trước' },
            { action: 'Hủy vé', movie: 'Dune 2', user: 'Lê Văn C', time: '30 phút trước' },
            { action: 'Đặt vé mới', movie: 'The Batman', user: 'Phạm Thị D', time: '1 giờ trước' },
          ].map((activity, index) => (
            <div key={index} className="flex items-center justify-between py-3 border-b last:border-0">
              <div>
                <p className="font-medium">{activity.action}</p>
                <p className="text-sm text-muted-foreground">
                  {activity.movie} - {activity.user}
                </p>
              </div>
              <span className="text-sm text-muted-foreground">{activity.time}</span>
            </div>
          ))}
        </div>
      </Card>
    </div>
  );
}

import { Card } from '@/app/components/ui/card';
import { Button } from '@/app/components/ui/button';
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/app/components/ui/tabs';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/app/components/ui/select';
import { Download, TrendingUp } from 'lucide-react';
import {
  BarChart,
  Bar,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from 'recharts';

const revenueByMonth = [
  { month: 'T1/2024', revenue: 180000000, tickets: 1420 },
  { month: 'T2/2024', revenue: 220000000, tickets: 1680 },
  { month: 'T3/2024', revenue: 195000000, tickets: 1520 },
  { month: 'T4/2024', revenue: 210000000, tickets: 1650 },
  { month: 'T5/2024', revenue: 245000000, tickets: 1890 },
  { month: 'T6/2024', revenue: 230000000, tickets: 1780 },
];

const revenueByMovie = [
  { movie: 'Oppenheimer', revenue: 324000000, tickets: 3240 },
  { movie: 'Barbie', revenue: 289000000, tickets: 2890 },
  { movie: 'Dune 2', revenue: 265000000, tickets: 2650 },
  { movie: 'The Batman', revenue: 234000000, tickets: 2340 },
  { movie: 'Killers...', revenue: 198000000, tickets: 1980 },
];

const revenueByCinema = [
  { cinema: 'CGV Vincom', revenue: 450000000, tickets: 4200 },
  { cinema: 'Lotte Landmark', revenue: 380000000, tickets: 3600 },
  { cinema: 'Galaxy Nguyễn Du', revenue: 320000000, tickets: 3100 },
];

export function ReportsPage() {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold mb-2">Báo cáo & Thống kê</h1>
          <p className="text-muted-foreground">Phân tích doanh thu và hiệu suất kinh doanh</p>
        </div>
        <div className="flex gap-3">
          <Select defaultValue="month">
            <SelectTrigger className="w-40">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="week">Tuần này</SelectItem>
              <SelectItem value="month">Tháng này</SelectItem>
              <SelectItem value="quarter">Quý này</SelectItem>
              <SelectItem value="year">Năm này</SelectItem>
            </SelectContent>
          </Select>
          <Button className="gap-2">
            <Download className="w-4 h-4" />
            Xuất báo cáo
          </Button>
        </div>
      </div>

      {/* Summary Cards */}
      <div className="grid gap-4 md:grid-cols-4">
        <Card className="p-4">
          <p className="text-sm text-muted-foreground mb-1">Doanh thu tháng này</p>
          <p className="text-2xl font-bold mb-2">245M đ</p>
          <div className="flex items-center gap-1 text-sm text-green-500">
            <TrendingUp className="w-4 h-4" />
            <span>+12.5%</span>
          </div>
        </Card>

        <Card className="p-4">
          <p className="text-sm text-muted-foreground mb-1">Vé bán được</p>
          <p className="text-2xl font-bold mb-2">1,890</p>
          <div className="flex items-center gap-1 text-sm text-green-500">
            <TrendingUp className="w-4 h-4" />
            <span>+8.3%</span>
          </div>
        </Card>

        <Card className="p-4">
          <p className="text-sm text-muted-foreground mb-1">Doanh thu trung bình/vé</p>
          <p className="text-2xl font-bold">129,630đ</p>
        </Card>

        <Card className="p-4">
          <p className="text-sm text-muted-foreground mb-1">Tỷ lệ lấp đầy</p>
          <p className="text-2xl font-bold">68%</p>
        </Card>
      </div>

      <Tabs defaultValue="revenue">
        <TabsList>
          <TabsTrigger value="revenue">Doanh thu</TabsTrigger>
          <TabsTrigger value="movies">Theo phim</TabsTrigger>
          <TabsTrigger value="cinemas">Theo rạp</TabsTrigger>
          <TabsTrigger value="compare">So sánh</TabsTrigger>
        </TabsList>

        {/* Revenue Tab */}
        <TabsContent value="revenue" className="space-y-6">
          <Card className="p-6">
            <h3 className="font-semibold mb-4">Biểu đồ doanh thu 6 tháng</h3>
            <ResponsiveContainer width="100%" height={400}>
              <LineChart data={revenueByMonth}>
                <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                <XAxis dataKey="month" stroke="hsl(var(--muted-foreground))" />
                <YAxis stroke="hsl(var(--muted-foreground))" />
                <Tooltip
                  contentStyle={{
                    backgroundColor: 'hsl(var(--card))',
                    border: '1px solid hsl(var(--border))',
                    borderRadius: '8px',
                  }}
                  formatter={(value: number, name: string) => [
                    name === 'revenue'
                      ? `${(value / 1000000).toFixed(0)}M đ`
                      : `${value} vé`,
                    name === 'revenue' ? 'Doanh thu' : 'Số vé',
                  ]}
                />
                <Legend />
                <Line
                  type="monotone"
                  dataKey="revenue"
                  stroke="hsl(var(--primary))"
                  strokeWidth={2}
                  name="Doanh thu"
                />
                <Line
                  type="monotone"
                  dataKey="tickets"
                  stroke="hsl(var(--chart-2))"
                  strokeWidth={2}
                  name="Số vé"
                />
              </LineChart>
            </ResponsiveContainer>
          </Card>
        </TabsContent>

        {/* Movies Tab */}
        <TabsContent value="movies" className="space-y-6">
          <Card className="p-6">
            <h3 className="font-semibold mb-4">Top 5 phim có doanh thu cao nhất</h3>
            <ResponsiveContainer width="100%" height={400}>
              <BarChart data={revenueByMovie} layout="vertical">
                <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                <XAxis type="number" stroke="hsl(var(--muted-foreground))" />
                <YAxis dataKey="movie" type="category" stroke="hsl(var(--muted-foreground))" />
                <Tooltip
                  contentStyle={{
                    backgroundColor: 'hsl(var(--card))',
                    border: '1px solid hsl(var(--border))',
                    borderRadius: '8px',
                  }}
                  formatter={(value: number) => [`${(value / 1000000).toFixed(0)}M đ`, 'Doanh thu']}
                />
                <Bar dataKey="revenue" fill="hsl(var(--primary))" radius={[0, 8, 8, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </Card>

          <Card className="p-6">
            <h3 className="font-semibold mb-4">Chi tiết doanh thu theo phim</h3>
            <div className="space-y-3">
              {revenueByMovie.map((movie, index) => (
                <div
                  key={index}
                  className="flex items-center justify-between p-4 rounded-lg bg-muted/30"
                >
                  <div className="flex-1">
                    <p className="font-semibold">{movie.movie}</p>
                    <p className="text-sm text-muted-foreground">{movie.tickets} vé đã bán</p>
                  </div>
                  <div className="text-right">
                    <p className="font-bold text-primary">
                      {(movie.revenue / 1000000).toFixed(0)}M đ
                    </p>
                    <p className="text-sm text-muted-foreground">
                      TB: {(movie.revenue / movie.tickets).toLocaleString('vi-VN')}đ/vé
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </Card>
        </TabsContent>

        {/* Cinemas Tab */}
        <TabsContent value="cinemas" className="space-y-6">
          <Card className="p-6">
            <h3 className="font-semibold mb-4">Doanh thu theo rạp chiếu</h3>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={revenueByCinema}>
                <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                <XAxis dataKey="cinema" stroke="hsl(var(--muted-foreground))" />
                <YAxis stroke="hsl(var(--muted-foreground))" />
                <Tooltip
                  contentStyle={{
                    backgroundColor: 'hsl(var(--card))',
                    border: '1px solid hsl(var(--border))',
                    borderRadius: '8px',
                  }}
                  formatter={(value: number) => [`${(value / 1000000).toFixed(0)}M đ`, 'Doanh thu']}
                />
                <Bar dataKey="revenue" fill="hsl(var(--chart-3))" radius={[8, 8, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </Card>

          <div className="grid md:grid-cols-3 gap-4">
            {revenueByCinema.map((cinema, index) => (
              <Card key={index} className="p-6">
                <h4 className="font-semibold mb-4">{cinema.cinema}</h4>
                <div className="space-y-2">
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">Doanh thu</span>
                    <span className="font-semibold">
                      {(cinema.revenue / 1000000).toFixed(0)}M đ
                    </span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">Số vé</span>
                    <span className="font-semibold">{cinema.tickets.toLocaleString('vi-VN')}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">TB/vé</span>
                    <span className="font-semibold">
                      {(cinema.revenue / cinema.tickets).toLocaleString('vi-VN')}đ
                    </span>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        </TabsContent>

        {/* Compare Tab */}
        <TabsContent value="compare" className="space-y-6">
          <Card className="p-6">
            <h3 className="font-semibold mb-4">So sánh doanh thu và số vé bán</h3>
            <ResponsiveContainer width="100%" height={400}>
              <BarChart data={revenueByMonth}>
                <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                <XAxis dataKey="month" stroke="hsl(var(--muted-foreground))" />
                <YAxis stroke="hsl(var(--muted-foreground))" />
                <Tooltip
                  contentStyle={{
                    backgroundColor: 'hsl(var(--card))',
                    border: '1px solid hsl(var(--border))',
                    borderRadius: '8px',
                  }}
                />
                <Legend />
                <Bar dataKey="revenue" fill="hsl(var(--primary))" name="Doanh thu (VNĐ)" />
                <Bar dataKey="tickets" fill="hsl(var(--chart-2))" name="Số vé" />
              </BarChart>
            </ResponsiveContainer>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}

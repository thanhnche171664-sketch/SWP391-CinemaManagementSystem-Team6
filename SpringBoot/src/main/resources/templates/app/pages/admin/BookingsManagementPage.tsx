import { mockBookings, mockShowtimes, mockMovies, mockCinemas } from '@/data/mockData';
import { Card } from '@/app/components/ui/card';
import { Input } from '@/app/components/ui/input';
import { Badge } from '@/app/components/ui/badge';
import { Button } from '@/app/components/ui/button';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/app/components/ui/table';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/app/components/ui/select';
import { Search, Eye, X, Download } from 'lucide-react';

export function BookingsManagementPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold mb-2">Quản lý đặt vé</h1>
        <p className="text-muted-foreground">Xem và quản lý tất cả đơn đặt vé</p>
      </div>

      {/* Filters */}
      <Card className="p-4">
        <div className="grid md:grid-cols-4 gap-4">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
            <Input type="text" placeholder="Tìm mã đặt vé..." className="pl-10" />
          </div>

          <Select defaultValue="all">
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">Tất cả trạng thái</SelectItem>
              <SelectItem value="confirmed">Đã xác nhận</SelectItem>
              <SelectItem value="pending">Đang chờ</SelectItem>
              <SelectItem value="cancelled">Đã hủy</SelectItem>
            </SelectContent>
          </Select>

          <Input type="date" />

          <Button variant="outline" className="gap-2">
            <Download className="w-4 h-4" />
            Xuất báo cáo
          </Button>
        </div>
      </Card>

      {/* Stats */}
      <div className="grid gap-4 md:grid-cols-4">
        <Card className="p-4">
          <p className="text-sm text-muted-foreground mb-1">Tổng đơn hàng</p>
          <p className="text-2xl font-bold">{mockBookings.length}</p>
        </Card>
        <Card className="p-4">
          <p className="text-sm text-muted-foreground mb-1">Đã xác nhận</p>
          <p className="text-2xl font-bold text-green-500">
            {mockBookings.filter((b) => b.status === 'confirmed').length}
          </p>
        </Card>
        <Card className="p-4">
          <p className="text-sm text-muted-foreground mb-1">Đang chờ</p>
          <p className="text-2xl font-bold text-yellow-500">0</p>
        </Card>
        <Card className="p-4">
          <p className="text-sm text-muted-foreground mb-1">Đã hủy</p>
          <p className="text-2xl font-bold text-red-500">
            {mockBookings.filter((b) => b.status === 'cancelled').length}
          </p>
        </Card>
      </div>

      {/* Bookings Table */}
      <Card>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Mã đặt vé</TableHead>
              <TableHead>Phim</TableHead>
              <TableHead>Rạp & Phòng</TableHead>
              <TableHead>Suất chiếu</TableHead>
              <TableHead>Ghế</TableHead>
              <TableHead>Tổng tiền</TableHead>
              <TableHead>Thanh toán</TableHead>
              <TableHead>Trạng thái</TableHead>
              <TableHead className="text-right">Thao tác</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {mockBookings.map((booking) => {
              const showtime = mockShowtimes.find((s) => s.id === booking.showtimeId);
              const movie = mockMovies.find((m) => m.id === showtime?.movieId);
              const cinema = mockCinemas.find((c) => c.id === showtime?.cinemaId);
              const room = cinema?.rooms.find((r) => r.id === showtime?.roomId);

              return (
                <TableRow key={booking.id}>
                  <TableCell className="font-mono">{booking.id}</TableCell>
                  <TableCell>
                    <div className="flex items-center gap-2">
                      <img
                        src={movie?.poster}
                        alt={movie?.title}
                        className="w-8 h-12 object-cover rounded"
                      />
                      <span className="font-medium">{movie?.title}</span>
                    </div>
                  </TableCell>
                  <TableCell>
                    <div>
                      <p className="font-medium">{cinema?.name}</p>
                      <p className="text-sm text-muted-foreground">{room?.name}</p>
                    </div>
                  </TableCell>
                  <TableCell>
                    <div>
                      <p>{new Date(showtime?.date || '').toLocaleDateString('vi-VN')}</p>
                      <p className="text-sm text-muted-foreground">{showtime?.time}</p>
                    </div>
                  </TableCell>
                  <TableCell>
                    <div className="flex flex-wrap gap-1">
                      {booking.seats.map((seat) => (
                        <Badge key={seat} variant="outline" className="text-xs">
                          {seat}
                        </Badge>
                      ))}
                    </div>
                  </TableCell>
                  <TableCell className="font-semibold">
                    {booking.totalPrice.toLocaleString('vi-VN')}đ
                  </TableCell>
                  <TableCell>
                    <Badge variant="outline">
                      {booking.paymentMethod === 'credit-card'
                        ? 'Thẻ'
                        : booking.paymentMethod === 'momo'
                        ? 'MoMo'
                        : 'ZaloPay'}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <Badge
                      variant={
                        booking.status === 'confirmed'
                          ? 'default'
                          : booking.status === 'pending'
                          ? 'secondary'
                          : 'destructive'
                      }
                    >
                      {booking.status === 'confirmed'
                        ? 'Đã xác nhận'
                        : booking.status === 'pending'
                        ? 'Đang chờ'
                        : 'Đã hủy'}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
                      <Button variant="ghost" size="icon">
                        <Eye className="w-4 h-4" />
                      </Button>
                      {booking.status === 'confirmed' && (
                        <Button variant="ghost" size="icon" className="text-destructive">
                          <X className="w-4 h-4" />
                        </Button>
                      )}
                    </div>
                  </TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </Card>
    </div>
  );
}

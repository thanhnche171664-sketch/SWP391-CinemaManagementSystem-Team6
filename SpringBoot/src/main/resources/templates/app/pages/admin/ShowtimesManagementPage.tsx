import { useState } from 'react';
import { mockShowtimes, mockMovies, mockCinemas } from '@/data/mockData';
import { Button } from '@/app/components/ui/button';
import { Card } from '@/app/components/ui/card';
import { Input } from '@/app/components/ui/input';
import { Label } from '@/app/components/ui/label';
import { Badge } from '@/app/components/ui/badge';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/app/components/ui/table';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/app/components/ui/dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/app/components/ui/select';
import { Plus, Edit, Trash2, Calendar } from 'lucide-react';

export function ShowtimesManagementPage() {
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [selectedDate, setSelectedDate] = useState('2024-01-20');

  const filteredShowtimes = mockShowtimes.filter((st) => st.date === selectedDate);

  const ShowtimeForm = () => (
    <div className="space-y-4 py-4">
      <div className="grid gap-4">
        <div>
          <Label htmlFor="movie">Phim *</Label>
          <Select>
            <SelectTrigger>
              <SelectValue placeholder="Chọn phim" />
            </SelectTrigger>
            <SelectContent>
              {mockMovies.map((movie) => (
                <SelectItem key={movie.id} value={movie.id}>
                  {movie.title}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div>
          <Label htmlFor="cinema">Rạp chiếu *</Label>
          <Select>
            <SelectTrigger>
              <SelectValue placeholder="Chọn rạp" />
            </SelectTrigger>
            <SelectContent>
              {mockCinemas.map((cinema) => (
                <SelectItem key={cinema.id} value={cinema.id}>
                  {cinema.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div>
          <Label htmlFor="room">Phòng chiếu *</Label>
          <Select>
            <SelectTrigger>
              <SelectValue placeholder="Chọn phòng" />
            </SelectTrigger>
            <SelectContent>
              {mockCinemas[0].rooms.map((room) => (
                <SelectItem key={room.id} value={room.id}>
                  {room.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <Label htmlFor="date">Ngày chiếu *</Label>
            <Input id="date" type="date" defaultValue="2024-01-20" />
          </div>

          <div>
            <Label htmlFor="time">Giờ chiếu *</Label>
            <Input id="time" type="time" defaultValue="10:00" />
          </div>
        </div>

        <div>
          <Label htmlFor="price">Giá vé (VNĐ) *</Label>
          <Input id="price" type="number" defaultValue="100000" />
        </div>
      </div>

      <div className="flex gap-2">
        <Button type="button" onClick={() => setIsDialogOpen(false)}>
          Thêm suất chiếu
        </Button>
        <Button type="button" variant="outline" onClick={() => setIsDialogOpen(false)}>
          Hủy
        </Button>
      </div>
    </div>
  );

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold mb-2">Quản lý suất chiếu</h1>
          <p className="text-muted-foreground">Thêm, sửa, xóa lịch chiếu phim</p>
        </div>

        <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
          <DialogTrigger asChild>
            <Button className="gap-2">
              <Plus className="w-4 h-4" />
              Thêm suất chiếu
            </Button>
          </DialogTrigger>
          <DialogContent className="max-w-lg">
            <DialogHeader>
              <DialogTitle>Thêm suất chiếu mới</DialogTitle>
            </DialogHeader>
            <ShowtimeForm />
          </DialogContent>
        </Dialog>
      </div>

      {/* Date Filter */}
      <Card className="p-4">
        <div className="flex items-center gap-4">
          <Calendar className="w-5 h-5 text-muted-foreground" />
          <Label htmlFor="filter-date">Chọn ngày:</Label>
          <Input
            id="filter-date"
            type="date"
            value={selectedDate}
            onChange={(e) => setSelectedDate(e.target.value)}
            className="max-w-xs"
          />
          <span className="text-sm text-muted-foreground">
            {filteredShowtimes.length} suất chiếu
          </span>
        </div>
      </Card>

      {/* Showtimes Table */}
      <Card>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Phim</TableHead>
              <TableHead>Rạp chiếu</TableHead>
              <TableHead>Phòng</TableHead>
              <TableHead>Ngày chiếu</TableHead>
              <TableHead>Giờ chiếu</TableHead>
              <TableHead>Giá vé</TableHead>
              <TableHead>Ghế trống</TableHead>
              <TableHead className="text-right">Thao tác</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filteredShowtimes.length > 0 ? (
              filteredShowtimes.map((showtime) => {
                const movie = mockMovies.find((m) => m.id === showtime.movieId);
                const cinema = mockCinemas.find((c) => c.id === showtime.cinemaId);
                const room = cinema?.rooms.find((r) => r.id === showtime.roomId);

                return (
                  <TableRow key={showtime.id}>
                    <TableCell>
                      <div className="flex items-center gap-3">
                        <img
                          src={movie?.poster}
                          alt={movie?.title}
                          className="w-10 h-14 object-cover rounded"
                        />
                        <div>
                          <p className="font-medium">{movie?.title}</p>
                          <p className="text-sm text-muted-foreground">
                            {movie?.duration} phút
                          </p>
                        </div>
                      </div>
                    </TableCell>
                    <TableCell>{cinema?.name}</TableCell>
                    <TableCell>{room?.name}</TableCell>
                    <TableCell>
                      {new Date(showtime.date).toLocaleDateString('vi-VN')}
                    </TableCell>
                    <TableCell>
                      <Badge variant="outline">{showtime.time}</Badge>
                    </TableCell>
                    <TableCell>{showtime.price.toLocaleString('vi-VN')}đ</TableCell>
                    <TableCell>
                      <Badge
                        variant={showtime.availableSeats > 20 ? 'default' : 'destructive'}
                      >
                        {showtime.availableSeats}/{room?.totalSeats}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-2">
                        <Button variant="ghost" size="icon">
                          <Edit className="w-4 h-4" />
                        </Button>
                        <Button variant="ghost" size="icon" className="text-destructive">
                          <Trash2 className="w-4 h-4" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                );
              })
            ) : (
              <TableRow>
                <TableCell colSpan={8} className="text-center py-8 text-muted-foreground">
                  Không có suất chiếu nào trong ngày này
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </Card>

      {/* Quick Actions */}
      <Card className="p-6">
        <h3 className="font-semibold mb-4">Thao tác nhanh</h3>
        <div className="flex flex-wrap gap-3">
          <Button variant="outline">Sao chép suất chiếu</Button>
          <Button variant="outline">Tạo lịch chiếu tự động</Button>
          <Button variant="outline">Xuất lịch chiếu</Button>
        </div>
      </Card>
    </div>
  );
}

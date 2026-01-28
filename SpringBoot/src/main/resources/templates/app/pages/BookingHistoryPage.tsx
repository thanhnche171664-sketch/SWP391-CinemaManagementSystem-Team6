import { mockBookings, mockShowtimes, mockMovies, mockCinemas } from '@/data/mockData';
import { Card } from '@/app/components/ui/card';
import { Button } from '@/app/components/ui/button';
import { Badge } from '@/app/components/ui/badge';
import { Calendar, Clock, MapPin, Ticket, Download } from 'lucide-react';

interface BookingHistoryPageProps {
  onViewTicket: (bookingId: string) => void;
}

export function BookingHistoryPage({ onViewTicket }: BookingHistoryPageProps) {
  const sortedBookings = [...mockBookings].sort(
    (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  );

  const getBookingDetails = (booking: typeof mockBookings[0]) => {
    const showtime = mockShowtimes.find((s) => s.id === booking.showtimeId);
    if (!showtime) return null;

    const movie = mockMovies.find((m) => m.id === showtime.movieId);
    const cinema = mockCinemas.find((c) => c.id === showtime.cinemaId);
    const room = cinema?.rooms.find((r) => r.id === showtime.roomId);

    return { showtime, movie, cinema, room };
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'confirmed':
        return 'bg-green-500/20 text-green-500 border-green-500/50';
      case 'cancelled':
        return 'bg-red-500/20 text-red-500 border-red-500/50';
      case 'pending':
        return 'bg-yellow-500/20 text-yellow-500 border-yellow-500/50';
      default:
        return 'bg-gray-500/20 text-gray-500 border-gray-500/50';
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'confirmed':
        return 'Đã xác nhận';
      case 'cancelled':
        return 'Đã hủy';
      case 'pending':
        return 'Đang chờ';
      default:
        return status;
    }
  };

  return (
    <div className="space-y-8 max-w-4xl mx-auto">
      <div>
        <h1 className="text-3xl font-bold mb-2">Lịch sử đặt vé</h1>
        <p className="text-muted-foreground">Quản lý và xem lại các vé đã đặt</p>
      </div>

      {sortedBookings.length > 0 ? (
        <div className="space-y-4">
          {sortedBookings.map((booking) => {
            const details = getBookingDetails(booking);
            if (!details) return null;

            const { showtime, movie, cinema, room } = details;
            if (!movie || !cinema) return null;

            return (
              <Card key={booking.id} className="p-6">
                <div className="flex flex-col md:flex-row gap-6">
                  {/* Movie Poster */}
                  <img
                    src={movie.poster}
                    alt={movie.title}
                    className="w-full md:w-32 h-48 object-cover rounded-lg"
                  />

                  {/* Booking Details */}
                  <div className="flex-1 space-y-4">
                    <div className="flex items-start justify-between gap-4">
                      <div>
                        <h3 className="text-xl font-semibold mb-1">{movie.title}</h3>
                        <Badge variant="outline">{movie.ageRating}</Badge>
                      </div>
                      <Badge className={getStatusColor(booking.status)}>
                        {getStatusText(booking.status)}
                      </Badge>
                    </div>

                    <div className="grid md:grid-cols-2 gap-4 text-sm">
                      <div className="space-y-2">
                        <div className="flex items-center gap-2">
                          <MapPin className="w-4 h-4 text-muted-foreground" />
                          <div>
                            <p className="font-medium">{cinema.name}</p>
                            <p className="text-muted-foreground">{room?.name}</p>
                          </div>
                        </div>

                        <div className="flex items-center gap-2">
                          <Calendar className="w-4 h-4 text-muted-foreground" />
                          <span>
                            {new Date(showtime.date).toLocaleDateString('vi-VN', {
                              weekday: 'long',
                              day: 'numeric',
                              month: 'long',
                              year: 'numeric',
                            })}
                          </span>
                        </div>

                        <div className="flex items-center gap-2">
                          <Clock className="w-4 h-4 text-muted-foreground" />
                          <span>{showtime.time}</span>
                        </div>
                      </div>

                      <div className="space-y-2">
                        <div className="flex items-center gap-2">
                          <Ticket className="w-4 h-4 text-muted-foreground" />
                          <span>Ghế: {booking.seats.join(', ')}</span>
                        </div>

                        <div>
                          <p className="text-muted-foreground mb-1">Tổng tiền</p>
                          <p className="text-2xl font-bold text-primary">
                            {booking.totalPrice.toLocaleString('vi-VN')}đ
                          </p>
                        </div>

                        <p className="text-xs text-muted-foreground">
                          Đặt lúc: {new Date(booking.createdAt).toLocaleString('vi-VN')}
                        </p>
                      </div>
                    </div>

                    <div className="flex gap-3 pt-2">
                      {booking.status === 'confirmed' && (
                        <>
                          <Button onClick={() => onViewTicket(booking.id)} className="gap-2">
                            <Ticket className="w-4 h-4" />
                            Xem vé
                          </Button>
                          <Button variant="outline" className="gap-2">
                            <Download className="w-4 h-4" />
                            Tải xuống
                          </Button>
                        </>
                      )}
                      {booking.status === 'pending' && (
                        <Button variant="destructive">Hủy đặt vé</Button>
                      )}
                    </div>
                  </div>
                </div>
              </Card>
            );
          })}
        </div>
      ) : (
        <Card className="p-12 text-center">
          <Ticket className="w-16 h-16 mx-auto mb-4 text-muted-foreground" />
          <h3 className="text-xl font-semibold mb-2">Chưa có vé nào</h3>
          <p className="text-muted-foreground mb-4">
            Bạn chưa đặt vé nào. Hãy chọn phim và đặt vé ngay!
          </p>
          <Button onClick={() => window.location.reload()}>Khám phá phim</Button>
        </Card>
      )}
    </div>
  );
}

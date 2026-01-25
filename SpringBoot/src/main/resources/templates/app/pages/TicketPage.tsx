import { Button } from '@/app/components/ui/button';
import { Card } from '@/app/components/ui/card';
import { Badge } from '@/app/components/ui/badge';
import { Separator } from '@/app/components/ui/separator';
import { CheckCircle, Download, Share2, MapPin, Calendar, Clock, Armchair } from 'lucide-react';

interface TicketPageProps {
  bookingId: string;
  onBackHome: () => void;
}

export function TicketPage({ bookingId, onBackHome }: TicketPageProps) {
  // Mock ticket data
  const ticket = {
    id: bookingId,
    movieTitle: 'Oppenheimer',
    poster: 'https://images.unsplash.com/photo-1594908900066-3f47337549d8?w=400',
    cinemaName: 'CGV Vincom Center',
    cinemaAddress: '72 Lê Thánh Tôn, Quận 1, TP.HCM',
    roomName: 'Phòng 1',
    date: '20/01/2024',
    time: '10:00',
    seats: ['A1', 'A2'],
    totalPrice: 200000,
    qrCode: 'https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=' + bookingId,
    status: 'valid',
  };

  const handlePrint = () => {
    window.print();
  };

  const handleShare = () => {
    if (navigator.share) {
      navigator.share({
        title: 'Vé xem phim',
        text: `Vé xem phim ${ticket.movieTitle} - ${ticket.date} ${ticket.time}`,
      });
    }
  };

  return (
    <div className="space-y-6 max-w-2xl mx-auto">
      {/* Success Message */}
      <Card className="p-8 text-center bg-gradient-to-br from-green-500/10 to-green-600/5 border-green-500/20">
        <CheckCircle className="w-16 h-16 text-green-500 mx-auto mb-4" />
        <h2 className="text-2xl font-bold mb-2">Đặt vé thành công!</h2>
        <p className="text-muted-foreground">
          Vé của bạn đã được đặt thành công. Vui lòng đến rạp trước giờ chiếu 15 phút.
        </p>
      </Card>

      {/* Ticket */}
      <Card className="overflow-hidden print:shadow-none">
        <div className="bg-gradient-to-br from-primary/20 to-primary/5 p-6">
          <div className="flex items-start gap-4">
            <img
              src={ticket.poster}
              alt={ticket.movieTitle}
              className="w-24 h-36 object-cover rounded-lg shadow-lg"
            />
            <div className="flex-1">
              <Badge className="mb-2">E-TICKET</Badge>
              <h3 className="text-2xl font-bold mb-2">{ticket.movieTitle}</h3>
              <p className="text-sm text-muted-foreground">Mã vé: {ticket.id}</p>
            </div>
          </div>
        </div>

        <div className="p-6 space-y-4">
          <div className="grid gap-4">
            <div className="flex items-start gap-3">
              <MapPin className="w-5 h-5 text-muted-foreground mt-0.5" />
              <div className="flex-1">
                <p className="font-semibold">{ticket.cinemaName}</p>
                <p className="text-sm text-muted-foreground">{ticket.cinemaAddress}</p>
                <p className="text-sm text-muted-foreground">{ticket.roomName}</p>
              </div>
            </div>

            <div className="flex items-center gap-3">
              <Calendar className="w-5 h-5 text-muted-foreground" />
              <div>
                <p className="font-semibold">Ngày chiếu</p>
                <p className="text-sm text-muted-foreground">{ticket.date}</p>
              </div>
            </div>

            <div className="flex items-center gap-3">
              <Clock className="w-5 h-5 text-muted-foreground" />
              <div>
                <p className="font-semibold">Giờ chiếu</p>
                <p className="text-sm text-muted-foreground">{ticket.time}</p>
              </div>
            </div>

            <div className="flex items-center gap-3">
              <Armchair className="w-5 h-5 text-muted-foreground" />
              <div>
                <p className="font-semibold">Ghế ngồi</p>
                <p className="text-sm text-muted-foreground">{ticket.seats.join(', ')}</p>
              </div>
            </div>
          </div>

          <Separator />

          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-muted-foreground">Tổng tiền</p>
              <p className="text-2xl font-bold text-primary">
                {ticket.totalPrice.toLocaleString('vi-VN')}đ
              </p>
            </div>
            <div className="text-right">
              <img
                src={ticket.qrCode}
                alt="QR Code"
                className="w-24 h-24 rounded border"
              />
              <p className="text-xs text-muted-foreground mt-1">Quét mã tại rạp</p>
            </div>
          </div>
        </div>

        <div className="bg-muted/30 p-4 text-center border-t border-dashed">
          <p className="text-sm text-muted-foreground">
            Vui lòng xuất trình mã QR này tại quầy vé trước giờ chiếu
          </p>
        </div>
      </Card>

      {/* Actions */}
      <div className="flex flex-wrap gap-3 print:hidden">
        <Button onClick={handlePrint} variant="outline" className="flex-1 gap-2">
          <Download className="w-4 h-4" />
          Tải vé
        </Button>
        <Button onClick={handleShare} variant="outline" className="flex-1 gap-2">
          <Share2 className="w-4 h-4" />
          Chia sẻ
        </Button>
        <Button onClick={onBackHome} className="flex-1">
          Về trang chủ
        </Button>
      </div>

      {/* Note */}
      <Card className="p-4 bg-muted/30 print:hidden">
        <h4 className="font-semibold mb-2">Lưu ý:</h4>
        <ul className="text-sm text-muted-foreground space-y-1 list-disc list-inside">
          <li>Vui lòng đến rạp trước giờ chiếu 15 phút</li>
          <li>Xuất trình mã QR tại quầy vé để nhận vé giấy</li>
          <li>Không được mang đồ ăn, thức uống từ bên ngoài vào rạp</li>
          <li>Vé đã mua không được hoàn lại hoặc đổi trả</li>
        </ul>
      </Card>
    </div>
  );
}

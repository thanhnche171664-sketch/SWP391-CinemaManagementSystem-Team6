import { useState } from 'react';
import { mockCinemas } from '@/data/mockData';
import { Button } from '@/app/components/ui/button';
import { Card } from '@/app/components/ui/card';
import { Input } from '@/app/components/ui/input';
import { Badge } from '@/app/components/ui/badge';
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/app/components/ui/tabs';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/app/components/ui/table';
import { Plus, Search, Edit, Trash2, Building2, DoorOpen } from 'lucide-react';

export function CinemasManagementPage() {
  const [searchQuery, setSearchQuery] = useState('');

  const filteredCinemas = mockCinemas.filter((cinema) =>
    cinema.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold mb-2">Quản lý rạp chiếu</h1>
          <p className="text-muted-foreground">Quản lý rạp, phòng chiếu và ghế ngồi</p>
        </div>
        <Button className="gap-2">
          <Plus className="w-4 h-4" />
          Thêm rạp mới
        </Button>
      </div>

      <Tabs defaultValue="cinemas">
        <TabsList>
          <TabsTrigger value="cinemas">Rạp chiếu</TabsTrigger>
          <TabsTrigger value="rooms">Phòng chiếu</TabsTrigger>
          <TabsTrigger value="seats">Cấu hình ghế</TabsTrigger>
        </TabsList>

        {/* Cinemas Tab */}
        <TabsContent value="cinemas" className="space-y-6">
          <div className="relative max-w-md">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
            <Input
              type="text"
              placeholder="Tìm kiếm rạp..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-10"
            />
          </div>

          <div className="grid gap-6">
            {filteredCinemas.map((cinema) => (
              <Card key={cinema.id} className="p-6">
                <div className="flex items-start justify-between mb-4">
                  <div className="flex items-start gap-4">
                    <div className="p-3 rounded-lg bg-primary/10">
                      <Building2 className="w-6 h-6 text-primary" />
                    </div>
                    <div>
                      <h3 className="text-xl font-bold mb-1">{cinema.name}</h3>
                      <p className="text-sm text-muted-foreground mb-2">{cinema.address}</p>
                      <div className="flex gap-4 text-sm">
                        <span className="text-muted-foreground">
                          <strong>Thành phố:</strong> {cinema.city}
                        </span>
                        <span className="text-muted-foreground">
                          <strong>SĐT:</strong> {cinema.phone}
                        </span>
                      </div>
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <Button variant="ghost" size="icon">
                      <Edit className="w-4 h-4" />
                    </Button>
                    <Button variant="ghost" size="icon" className="text-destructive">
                      <Trash2 className="w-4 h-4" />
                    </Button>
                  </div>
                </div>

                <div className="border-t pt-4">
                  <h4 className="font-semibold mb-3">Phòng chiếu ({cinema.rooms.length})</h4>
                  <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-3">
                    {cinema.rooms.map((room) => (
                      <div
                        key={room.id}
                        className="flex items-center gap-3 p-3 rounded-lg bg-muted/50"
                      >
                        <DoorOpen className="w-5 h-5 text-muted-foreground" />
                        <div className="flex-1">
                          <p className="font-medium">{room.name}</p>
                          <p className="text-sm text-muted-foreground">
                            {room.totalSeats} ghế
                          </p>
                        </div>
                        <Button variant="ghost" size="icon">
                          <Edit className="w-4 h-4" />
                        </Button>
                      </div>
                    ))}
                  </div>
                </div>
              </Card>
            ))}
          </div>
        </TabsContent>

        {/* Rooms Tab */}
        <TabsContent value="rooms" className="space-y-6">
          <div className="flex justify-between items-center">
            <div className="relative max-w-md">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
              <Input type="text" placeholder="Tìm kiếm phòng..." className="pl-10" />
            </div>
            <Button className="gap-2">
              <Plus className="w-4 h-4" />
              Thêm phòng mới
            </Button>
          </div>

          <Card>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Tên phòng</TableHead>
                  <TableHead>Rạp chiếu</TableHead>
                  <TableHead>Tổng ghế</TableHead>
                  <TableHead>Hàng x Cột</TableHead>
                  <TableHead>Trạng thái</TableHead>
                  <TableHead className="text-right">Thao tác</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {mockCinemas.flatMap((cinema) =>
                  cinema.rooms.map((room) => (
                    <TableRow key={room.id}>
                      <TableCell className="font-medium">{room.name}</TableCell>
                      <TableCell>{cinema.name}</TableCell>
                      <TableCell>{room.totalSeats} ghế</TableCell>
                      <TableCell>
                        {room.rows} x {room.columns}
                      </TableCell>
                      <TableCell>
                        <Badge>Hoạt động</Badge>
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
                  ))
                )}
              </TableBody>
            </Table>
          </Card>
        </TabsContent>

        {/* Seats Tab */}
        <TabsContent value="seats" className="space-y-6">
          <Card className="p-6">
            <h3 className="font-semibold mb-4">Cấu hình loại ghế và giá</h3>
            <div className="space-y-4">
              <div className="grid md:grid-cols-3 gap-4">
                <Card className="p-4 border-2 border-primary/20">
                  <h4 className="font-semibold mb-2">Ghế thường</h4>
                  <div className="space-y-2">
                    <div>
                      <label className="text-sm text-muted-foreground">Giá vé</label>
                      <Input type="number" defaultValue="100000" />
                    </div>
                    <p className="text-sm text-muted-foreground">
                      Ghế tiêu chuẩn, phù hợp cho đại đa số khách hàng
                    </p>
                  </div>
                </Card>

                <Card className="p-4 border-2 border-amber-500/20">
                  <h4 className="font-semibold mb-2">Ghế VIP</h4>
                  <div className="space-y-2">
                    <div>
                      <label className="text-sm text-muted-foreground">Giá vé</label>
                      <Input type="number" defaultValue="150000" />
                    </div>
                    <p className="text-sm text-muted-foreground">
                      Ghế cao cấp, thoải mái hơn, thường ở vị trí đẹp
                    </p>
                  </div>
                </Card>

                <Card className="p-4 border-2 border-pink-500/20">
                  <h4 className="font-semibold mb-2">Ghế đôi</h4>
                  <div className="space-y-2">
                    <div>
                      <label className="text-sm text-muted-foreground">Giá vé</label>
                      <Input type="number" defaultValue="180000" />
                    </div>
                    <p className="text-sm text-muted-foreground">
                      Ghế đôi dành cho cặp đôi, có tay vịn có thể gập
                    </p>
                  </div>
                </Card>
              </div>

              <Button>Lưu cấu hình</Button>
            </div>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}

import { useState } from 'react';
import { mockMovies } from '@/data/mockData';
import { Button } from '@/app/components/ui/button';
import { Card } from '@/app/components/ui/card';
import { Input } from '@/app/components/ui/input';
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
import { Label } from '@/app/components/ui/label';
import { Textarea } from '@/app/components/ui/textarea';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/app/components/ui/select';
import { Plus, Search, Edit, Trash2, Eye } from 'lucide-react';

export function MoviesManagementPage() {
  const [searchQuery, setSearchQuery] = useState('');
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [editingMovie, setEditingMovie] = useState<any>(null);

  const filteredMovies = mockMovies.filter((movie) =>
    movie.title.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const handleEdit = (movie: any) => {
    setEditingMovie(movie);
    setIsDialogOpen(true);
  };

  const handleAdd = () => {
    setEditingMovie(null);
    setIsDialogOpen(true);
  };

  const MovieForm = () => (
    <div className="space-y-4 py-4">
      <div className="grid gap-4">
        <div>
          <Label htmlFor="title">Tên phim *</Label>
          <Input id="title" defaultValue={editingMovie?.title} placeholder="Nhập tên phim" />
        </div>

        <div>
          <Label htmlFor="originalTitle">Tên gốc</Label>
          <Input
            id="originalTitle"
            defaultValue={editingMovie?.originalTitle}
            placeholder="Tên phim gốc"
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <Label htmlFor="duration">Thời lượng (phút) *</Label>
            <Input
              id="duration"
              type="number"
              defaultValue={editingMovie?.duration}
              placeholder="120"
            />
          </div>

          <div>
            <Label htmlFor="ageRating">Độ tuổi *</Label>
            <Select defaultValue={editingMovie?.ageRating || 'PG-13'}>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="P">P - Phổ thông</SelectItem>
                <SelectItem value="K">K - Dành cho trẻ em</SelectItem>
                <SelectItem value="T13">T13 - Từ 13 tuổi</SelectItem>
                <SelectItem value="T16">T16 - Từ 16 tuổi</SelectItem>
                <SelectItem value="T18">T18 - Từ 18 tuổi</SelectItem>
                <SelectItem value="C">C - Cấm</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>

        <div>
          <Label htmlFor="releaseDate">Ngày khởi chiếu *</Label>
          <Input
            id="releaseDate"
            type="date"
            defaultValue={editingMovie?.releaseDate}
          />
        </div>

        <div>
          <Label htmlFor="status">Trạng thái *</Label>
          <Select defaultValue={editingMovie?.status || 'now-showing'}>
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="now-showing">Đang chiếu</SelectItem>
              <SelectItem value="coming-soon">Sắp chiếu</SelectItem>
              <SelectItem value="ended">Đã kết thúc</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div>
          <Label htmlFor="director">Đạo diễn *</Label>
          <Input id="director" defaultValue={editingMovie?.director} placeholder="Tên đạo diễn" />
        </div>

        <div>
          <Label htmlFor="cast">Diễn viên (phân cách bằng dấu phẩy)</Label>
          <Input
            id="cast"
            defaultValue={editingMovie?.cast?.join(', ')}
            placeholder="Diễn viên 1, Diễn viên 2,..."
          />
        </div>

        <div>
          <Label htmlFor="genre">Thể loại (phân cách bằng dấu phẩy)</Label>
          <Input
            id="genre"
            defaultValue={editingMovie?.genre?.join(', ')}
            placeholder="Action, Drama, ..."
          />
        </div>

        <div>
          <Label htmlFor="description">Mô tả *</Label>
          <Textarea
            id="description"
            defaultValue={editingMovie?.description}
            placeholder="Mô tả nội dung phim"
            rows={4}
          />
        </div>

        <div>
          <Label htmlFor="poster">URL Poster</Label>
          <Input id="poster" defaultValue={editingMovie?.poster} placeholder="https://..." />
        </div>
      </div>

      <div className="flex gap-2">
        <Button type="button" onClick={() => setIsDialogOpen(false)}>
          {editingMovie ? 'Cập nhật' : 'Thêm phim'}
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
          <h1 className="text-3xl font-bold mb-2">Quản lý phim</h1>
          <p className="text-muted-foreground">Thêm, sửa, xóa thông tin phim</p>
        </div>

        <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
          <DialogTrigger asChild>
            <Button onClick={handleAdd} className="gap-2">
              <Plus className="w-4 h-4" />
              Thêm phim mới
            </Button>
          </DialogTrigger>
          <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
            <DialogHeader>
              <DialogTitle>{editingMovie ? 'Chỉnh sửa phim' : 'Thêm phim mới'}</DialogTitle>
            </DialogHeader>
            <MovieForm />
          </DialogContent>
        </Dialog>
      </div>

      {/* Search */}
      <div className="relative max-w-md">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
        <Input
          type="text"
          placeholder="Tìm kiếm phim..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="pl-10"
        />
      </div>

      {/* Table */}
      <Card>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Poster</TableHead>
              <TableHead>Tên phim</TableHead>
              <TableHead>Thể loại</TableHead>
              <TableHead>Thời lượng</TableHead>
              <TableHead>Đánh giá</TableHead>
              <TableHead>Trạng thái</TableHead>
              <TableHead className="text-right">Thao tác</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filteredMovies.map((movie) => (
              <TableRow key={movie.id}>
                <TableCell>
                  <img
                    src={movie.poster}
                    alt={movie.title}
                    className="w-12 h-16 object-cover rounded"
                  />
                </TableCell>
                <TableCell>
                  <div>
                    <p className="font-medium">{movie.title}</p>
                    <p className="text-sm text-muted-foreground">{movie.originalTitle}</p>
                  </div>
                </TableCell>
                <TableCell>
                  <div className="flex flex-wrap gap-1">
                    {movie.genre.slice(0, 2).map((g) => (
                      <Badge key={g} variant="outline" className="text-xs">
                        {g}
                      </Badge>
                    ))}
                  </div>
                </TableCell>
                <TableCell>{movie.duration} phút</TableCell>
                <TableCell>{movie.rating}/10</TableCell>
                <TableCell>
                  <Badge
                    variant={
                      movie.status === 'now-showing'
                        ? 'default'
                        : movie.status === 'coming-soon'
                        ? 'secondary'
                        : 'outline'
                    }
                  >
                    {movie.status === 'now-showing'
                      ? 'Đang chiếu'
                      : movie.status === 'coming-soon'
                      ? 'Sắp chiếu'
                      : 'Đã kết thúc'}
                  </Badge>
                </TableCell>
                <TableCell className="text-right">
                  <div className="flex justify-end gap-2">
                    <Button variant="ghost" size="icon">
                      <Eye className="w-4 h-4" />
                    </Button>
                    <Button variant="ghost" size="icon" onClick={() => handleEdit(movie)}>
                      <Edit className="w-4 h-4" />
                    </Button>
                    <Button variant="ghost" size="icon" className="text-destructive">
                      <Trash2 className="w-4 h-4" />
                    </Button>
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Card>
    </div>
  );
}

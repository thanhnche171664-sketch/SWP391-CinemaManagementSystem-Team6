import { useState, useMemo } from 'react';
import { Movie } from '@/types';
import { mockMovies } from '@/data/mockData';
import { MovieCard } from '@/app/components/MovieCard';
import { Input } from '@/app/components/ui/input';
import { Button } from '@/app/components/ui/button';
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/app/components/ui/tabs';
import { Search, Filter } from 'lucide-react';

interface HomePageProps {
  onMovieClick: (movieId: string) => void;
}

export function HomePage({ onMovieClick }: HomePageProps) {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedTab, setSelectedTab] = useState('all');
  const [selectedGenre, setSelectedGenre] = useState('all');

  // Lấy danh sách tất cả thể loại từ movies
  const allGenres = useMemo(() => {
    const genresSet = new Set<string>();
    mockMovies.forEach((movie) => {
      movie.genre.forEach((genre) => genresSet.add(genre));
    });
    return Array.from(genresSet).sort();
  }, []);

  const filteredMovies = mockMovies.filter((movie) => {
    const matchesSearch = movie.title.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesTab =
      selectedTab === 'all' ||
      (selectedTab === 'now-showing' && movie.status === 'now-showing') ||
      (selectedTab === 'coming-soon' && movie.status === 'coming-soon');
    const matchesGenre = selectedGenre === 'all' || movie.genre.includes(selectedGenre);

    return matchesSearch && matchesTab && matchesGenre;
  });

  return (
    <div className="space-y-8">
      {/* Hero Section */}
      <div className="relative h-[400px] md:h-[500px] rounded-2xl overflow-hidden">
        <img
          src="https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=1200"
          alt="Cinema"
          className="w-full h-full object-cover"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-background via-background/50 to-transparent" />
        <div className="absolute bottom-0 left-0 right-0 p-8 md:p-12">
          <h1 className="text-4xl md:text-6xl font-bold mb-4">
            Trải nghiệm điện ảnh đỉnh cao
          </h1>
          <p className="text-lg text-muted-foreground max-w-2xl mb-6">
            Đặt vé xem phim nhanh chóng, tiện lợi với CinemaHub
          </p>
        </div>
      </div>

      {/* Search Bar */}
      <div className="relative max-w-2xl mx-auto">
        <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
        <Input
          type="text"
          placeholder="Tìm kiếm phim..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="pl-12 h-14 bg-card border-border"
        />
      </div>

      {/* Filter Section */}
      <div className="flex flex-col md:flex-row gap-4 items-start md:items-center justify-between">
        <Tabs value={selectedTab} onValueChange={setSelectedTab}>
          <TabsList className="w-full md:w-auto">
            <TabsTrigger value="all">Tất cả</TabsTrigger>
            <TabsTrigger value="now-showing">Đang chiếu</TabsTrigger>
            <TabsTrigger value="coming-soon">Sắp chiếu</TabsTrigger>
          </TabsList>
        </Tabs>

        {/* Genre Filter using HTML */}
        <div className="flex items-center gap-3 w-full md:w-auto">
          <Filter className="w-5 h-5 text-muted-foreground" />
          <label htmlFor="genre-filter" className="text-sm font-medium">
            Thể loại:
          </label>
          <select
            id="genre-filter"
            value={selectedGenre}
            onChange={(e) => setSelectedGenre(e.target.value)}
            className="flex h-10 w-full md:w-[200px] items-center justify-between rounded-md border border-border bg-background px-3 py-2 text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 focus:ring-offset-background disabled:cursor-not-allowed disabled:opacity-50 cursor-pointer"
            style={{
              backgroundColor: 'hsl(var(--background))',
              color: 'hsl(var(--foreground))',
            }}
          >
            <option value="all" style={{ backgroundColor: 'hsl(var(--background))', color: 'hsl(var(--foreground))' }}>
              Tất cả thể loại
            </option>
            {allGenres.map((genre) => (
              <option 
                key={genre} 
                value={genre}
                style={{ backgroundColor: 'hsl(var(--background))', color: 'hsl(var(--foreground))' }}
              >
                {genre}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Movies Grid */}
      <div className="mt-6">
        {filteredMovies.length > 0 ? (
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4 md:gap-6">
            {filteredMovies.map((movie) => (
              <MovieCard
                key={movie.id}
                movie={movie}
                onClick={() => onMovieClick(movie.id)}
              />
            ))}
          </div>
        ) : (
          <div className="text-center py-12">
            <p className="text-muted-foreground">Không tìm thấy phim nào</p>
            <button
              onClick={() => {
                setSearchQuery('');
                setSelectedTab('all');
                setSelectedGenre('all');
              }}
              className="mt-4 px-4 py-2 rounded-md bg-primary text-primary-foreground hover:bg-primary/90 transition-colors"
            >
              Xóa bộ lọc
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
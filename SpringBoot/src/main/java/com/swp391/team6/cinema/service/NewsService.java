package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.entity.News;
import com.swp391.team6.cinema.entity.User;
import com.swp391.team6.cinema.repository.CinemaBranchRepository;
import com.swp391.team6.cinema.repository.NewsRepository;
import com.swp391.team6.cinema.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;
    private final UserRepository userRepository;
    private final CinemaBranchRepository cinemaBranchRepository;

    @Transactional(readOnly = true)
    public List<News> getLatestPublishedNews(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "publishedAt", "newsId"));
        return newsRepository.searchPublished(News.NewsStatus.published, null, null, null, pageable).getContent();
    }

    @Transactional(readOnly = true)
    public Page<News> getPublicNews(String keyword,
                                    News.NewsType newsType,
                                    Long branchId,
                                    String sort,
                                    int page,
                                    int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), size, buildSort(sort));
        return newsRepository.searchPublished(News.NewsStatus.published, keyword, newsType, branchId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<News> getNewsForManagement(User user,
                                           String keyword,
                                           News.NewsType newsType,
                                           News.NewsStatus newsStatus,
                                           String sort,
                                           int page,
                                           int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), size, buildSort(sort));
        Long branchFilter = user.getRole() == User.UserRole.MANAGER ? user.getBranchId() : null;
        return newsRepository.searchForManagement(keyword, newsType, newsStatus, branchFilter, pageable);
    }

    @Transactional(readOnly = true)
    public News getPublishedNewsById(Long newsId) {
        return newsRepository.findWithAuthorAndBranchByNewsIdAndStatus(newsId, News.NewsStatus.published)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tức đã xuất bản."));
    }

    @Transactional(readOnly = true)
    public News getNewsForEdit(Long newsId, User user) {
        News news = newsRepository.findWithAuthorAndBranchByNewsId(newsId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tức."));
        validateManagementPermission(news, user);
        return news;
    }

    @Transactional
    public void createNews(News news, User user) {
        validateAuthorAndRole(user);
        validateRequiredFields(news);
        validatePublishedRulesForCreate(news);
        validateNoDuplicateTitleOnCreate(news.getTitle());
        news.setNewsId(null);
        news.setAuthorId(user.getUserId());
        news.setBranchId(resolveAndValidateBranchId(news.getBranchId(), user));
        newsRepository.save(news);
    }

    @Transactional
    public void updateNews(Long newsId, News request, User user) {
        News existing = getNewsForEdit(newsId, user);
        validateAuthorAndRole(user);
        validateRequiredFields(request);
        validateNoDuplicateTitleOnUpdate(request.getTitle(), newsId);

        News.NewsStatus oldStatus = existing.getStatus();
        News.NewsStatus newStatus = request.getStatus() != null ? request.getStatus() : News.NewsStatus.draft;

        existing.setTitle(request.getTitle().trim());
        existing.setContent(request.getContent().trim());
        existing.setThumbnailUrl(trimToNull(request.getThumbnailUrl()));
        existing.setNewsType(request.getNewsType());

        if (oldStatus == News.NewsStatus.published) {
            if (request.getPublishedAt() == null || !request.getPublishedAt().equals(existing.getPublishedAt())) {
                throw new RuntimeException("Tin đã publish thì không được thay đổi published_at.");
            }
            existing.setPublishedAt(existing.getPublishedAt());
        } else {
            if (newStatus == News.NewsStatus.published && request.getPublishedAt() == null) {
                throw new RuntimeException("published_at không được để trống khi status = published.");
            }
            existing.setPublishedAt(request.getPublishedAt());
        }

        existing.setStatus(newStatus);
        existing.setBranchId(resolveAndValidateBranchId(request.getBranchId(), user));

        newsRepository.save(existing);
    }

    @Transactional
    public void hideNews(Long newsId, User user) {
        News news = getNewsForEdit(newsId, user);
        news.setStatus(News.NewsStatus.hidden);
        newsRepository.save(news);
    }

    private void validateManagementPermission(News news, User user) {
        if (user.getRole() == User.UserRole.ADMIN) {
            return;
        }
        if (user.getRole() == User.UserRole.MANAGER && user.getBranchId() != null && user.getBranchId().equals(news.getBranchId())) {
            return;
        }
        throw new RuntimeException("Bạn không có quyền thao tác tin tức này.");
    }

    private void validateRequiredFields(News news) {
        if (news.getTitle() == null) {
            throw new RuntimeException("Tiêu đề không được để trống.");
        }
        String title = news.getTitle().trim();
        if (title.isEmpty()) {
            throw new RuntimeException("Tiêu đề không được để trống.");
        }
        if (title.length() > 255) {
            throw new RuntimeException("Tiêu đề tối đa 255 ký tự.");
        }
        news.setTitle(title);

        if (news.getContent() == null) {
            throw new RuntimeException("Nội dung không được để trống.");
        }
        String content = news.getContent().trim();
        if (content.isEmpty()) {
            throw new RuntimeException("Nội dung không được để trống.");
        }
        if (content.length() <= 10) {
            throw new RuntimeException("Nội dung phải lớn hơn 10 ký tự.");
        }
        news.setContent(content);

        if (news.getNewsType() == null) {
            news.setNewsType(News.NewsType.announcement);
        }
        if (news.getStatus() == null) {
            news.setStatus(News.NewsStatus.draft);
        }
        news.setThumbnailUrl(trimToNull(news.getThumbnailUrl()));
    }

    private void validateAuthorAndRole(User user) {
        if (user == null || user.getUserId() == null) {
            throw new RuntimeException("Không xác định được tác giả.");
        }
        User persisted = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new RuntimeException("author_id không tồn tại trong users."));
        if (persisted.getRole() != User.UserRole.ADMIN && persisted.getRole() != User.UserRole.MANAGER) {
            throw new RuntimeException("Chỉ ADMIN hoặc MANAGER mới được đăng news.");
        }
    }

    private Long resolveAndValidateBranchId(Long requestedBranchId, User user) {
        if (user.getRole() == User.UserRole.MANAGER) {
            if (user.getBranchId() == null) {
                throw new RuntimeException("Manager chưa được gán chi nhánh.");
            }
            if (!cinemaBranchRepository.existsById(user.getBranchId())) {
                throw new RuntimeException("Chi nhánh của manager không tồn tại.");
            }
            return user.getBranchId();
        }
        if (requestedBranchId == null) {
            return null;
        }
        if (!cinemaBranchRepository.existsById(requestedBranchId)) {
            throw new RuntimeException("branch_id không tồn tại trong cinema_branches.");
        }
        return requestedBranchId;
    }

    private void validatePublishedRulesForCreate(News news) {
        if (news.getStatus() == News.NewsStatus.published && news.getPublishedAt() == null) {
            throw new RuntimeException("published_at không được để trống khi status = published.");
        }
    }

    private void validateNoDuplicateTitleOnCreate(String title) {
        if (newsRepository.existsByTitleIgnoreCase(title.trim())) {
            throw new RuntimeException("Tiêu đề tin đã tồn tại, vui lòng đặt tiêu đề khác.");
        }
    }

    private void validateNoDuplicateTitleOnUpdate(String title, Long newsId) {
        if (newsRepository.existsByTitleIgnoreCaseAndNewsIdNot(title.trim(), newsId)) {
            throw new RuntimeException("Tiêu đề tin đã tồn tại, vui lòng đặt tiêu đề khác.");
        }
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Sort buildSort(String sort) {
        if ("oldest".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.ASC, "publishedAt", "newsId");
        }
        if ("title_asc".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.ASC, "title");
        }
        if ("title_desc".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.DESC, "title");
        }
        return Sort.by(Sort.Direction.DESC, "publishedAt", "newsId");
    }
}

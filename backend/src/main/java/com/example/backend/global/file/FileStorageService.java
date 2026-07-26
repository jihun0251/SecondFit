package com.example.backend.global.file;

import com.example.backend.global.exception.BusinessException;
import com.example.backend.global.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 상품 이미지 로컬 디스크 저장.
 * <p>
 * 운영 환경이라면 S3 + CloudFront로 가야 하지만,
 * 개인 프로젝트 단계에서는 로컬 디렉터리에 저장하고 정적 리소스로 서빙하는 것으로 충분하다.
 * (교체 지점을 이 클래스 하나로 묶어뒀기 때문에 나중에 S3로 바꿔도 호출부는 그대로다)
 */
@Slf4j
@Service
public class FileStorageService {

    /** 확장자 화이트리스트 — 실행 가능한 파일(.jsp, .html 등)이 올라오는 걸 막는다 */
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "webp", "gif");

    /** 브라우저가 접근할 URL 경로 prefix (WebMvcConfig의 리소스 핸들러와 짝을 이룬다) */
    public static final String URL_PREFIX = "/uploads/";

    private final Path uploadRoot;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    void init() {
        try {
            Files.createDirectories(uploadRoot);
            log.info("업로드 디렉터리: {}", uploadRoot);
        } catch (IOException e) {
            throw new IllegalStateException("업로드 디렉터리를 만들 수 없습니다: " + uploadRoot, e);
        }
    }

    /**
     * 파일 저장 후 접근용 URL을 반환한다.
     *
     * @return 예) "/uploads/3f2a....jpg"
     */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }

        String extension = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }

        // 원본 파일명을 그대로 쓰면 덮어쓰기·한글깨짐·경로조작(../) 위험이 있다 → UUID로 새 이름 부여
        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path target = uploadRoot.resolve(storedName).normalize();

        // 경로 조작 방어: 저장 경로가 업로드 루트를 벗어나면 거부
        if (!target.startsWith(uploadRoot)) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("파일 저장 실패: {}", storedName, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        return URL_PREFIX + storedName;
    }

    private String extractExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }
        String ext = StringUtils.getFilenameExtension(originalFilename);
        if (!StringUtils.hasText(ext)) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }
        return ext.toLowerCase(Locale.ROOT);
    }
}

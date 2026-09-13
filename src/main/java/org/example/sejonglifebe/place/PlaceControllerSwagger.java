package org.example.sejonglifebe.place;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.common.dto.CommonResponse;
import org.example.sejonglifebe.external.dto.MapLinksRequest;
import org.example.sejonglifebe.external.dto.MapLinksResponse;
import org.example.sejonglifebe.external.dto.PlaceSearchResponse;
import org.example.sejonglifebe.place.dto.PlaceDetailResponse;
import org.example.sejonglifebe.place.dto.PlacePageResponse;
import org.example.sejonglifebe.place.dto.PlaceRequest;
import org.example.sejonglifebe.place.dto.PlaceResponse;
import org.example.sejonglifebe.place.dto.PlaceSearchConditions;
import org.example.sejonglifebe.place.dto.PlaceUpdateRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Place", description = "장소")
public interface PlaceControllerSwagger {

    @Operation(summary = "장소 목록 조회")
    ResponseEntity<CommonResponse<PlacePageResponse>> getPlaces(
            @Valid @ModelAttribute PlaceSearchConditions conditions,
            Pageable pageable);

    @Operation(summary = "주간 핫플레이스 조회")
    ResponseEntity<CommonResponse<List<PlaceResponse>>> getHotPlaces();

    @Operation(summary = "장소 상세 정보 조회")
    ResponseEntity<CommonResponse<PlaceDetailResponse>> getPlaceDetail(
            @PathVariable Long placeId,
            AuthUser authUser,
            HttpServletRequest request);

    @Operation(summary = "장소 추가")
    ResponseEntity<CommonResponse<Void>> createPlace(
            @Valid @RequestPart("place") PlaceRequest placeRequest,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            AuthUser authUser
    );

    @Operation(summary = "장소 수정", description = "관리자 전용. multipart/form-data의 필수 place 파트에 장소 정보 전체를 application/json으로 전달합니다. "
            + "위도·경도는 함께 전달하거나 둘 다 null로 전달합니다. "
            + "내용이 있는 thumbnail 파일을 보내면 WebP로 변환해 추가 또는 교체합니다. "
            + "생략하거나 빈 파일을 보내면 파일 없음으로 처리하며 삭제 요청이 없으면 기존 썸네일을 유지합니다. "
            + "deleteThumbnail=true이면 관리자 썸네일만 삭제합니다. 내용이 있는 파일과 삭제 요청은 동시에 보낼 수 없습니다. "
            + "리뷰 사진은 유지되며 관리자 썸네일이 없으면 기존 첫 사진을 대표 이미지로 사용합니다.")
    ResponseEntity<CommonResponse<Void>> updatePlace(
            @PathVariable("placeId") Long placeId,
            @Valid @RequestPart("place") PlaceUpdateRequest placeRequest,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestParam(value = "deleteThumbnail", defaultValue = "false") boolean deleteThumbnail
    );

    @Operation(summary = "장소 삭제")
    ResponseEntity<CommonResponse<Void>> deletePlace(
            @PathVariable Long placeId,
            AuthUser authUser
    );

    @Operation(summary = "추가할 장소 url 생성")
    ResponseEntity<CommonResponse<MapLinksResponse>> buildUrl(@RequestBody MapLinksRequest request);

    @Operation(summary = "추가할 장소명 검색")
    ResponseEntity<CommonResponse<List<PlaceSearchResponse>>> search(@RequestParam("query") String query);

    @Operation(summary = "내 즐겨찾기 목록 조회")
    ResponseEntity<CommonResponse<List<PlaceResponse>>> getMyFavoritePlaces(
            AuthUser authUser
    );

    @Operation(summary = "장소 즐겨찾기 추가")
    ResponseEntity<CommonResponse<Void>> addFavoritePlace(
            @PathVariable Long placeId,
            AuthUser authUser
    );

    @Operation(summary = "장소 즐겨찾기 삭제")
    ResponseEntity<CommonResponse<Void>> removeFavoritePlace(
            @PathVariable Long placeId,
            AuthUser authUser
    );

    @Operation(summary = "내 즐겨찾기 개수 조회")
    ResponseEntity<CommonResponse<Long>> getMyFavoriteCount(
            AuthUser authUser
    );
}

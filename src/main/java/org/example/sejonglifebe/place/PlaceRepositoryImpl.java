package org.example.sejonglifebe.place;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.tag.Tag;
import org.springframework.util.StringUtils;

import java.util.List;

import static org.example.sejonglifebe.place.entity.QPlace.*;
import static org.example.sejonglifebe.place.entity.QPlaceCategory.*;
import static org.example.sejonglifebe.place.entity.QPlaceTag.*;
import static org.example.sejonglifebe.review.QReview.*;

@RequiredArgsConstructor
public class PlaceRepositoryImpl implements PlaceRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Place> getPlacesByConditions(Category category, List<Tag> tags, String keyword) {
        JPAQuery<Place> query = queryFactory.selectFrom(place);

        if (category != null) {
            query = query.leftJoin(place.placeCategories, placeCategory);
        }

        if (tags != null && !tags.isEmpty()) {
            query = query.leftJoin(place.placeTags, placeTag);
        }

        return query
                .leftJoin(place.reviews, review)
                .where(likePlaceName(keyword),
                        placeCategoryEq(category),
                        placeTagIn(tags)
                )
                .groupBy(place.id)
                .having(placeTagCountEq(tags))
                .orderBy(review.count().desc())
                .fetch();

    }

    private BooleanExpression likePlaceName(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return place.name.like("%" + keyword + "%");
    }

    private BooleanExpression placeCategoryEq(Category category) {
        if (category == null) {
            return null;
        }
        return placeCategory.category.eq(category);
    }

    private BooleanExpression placeTagIn(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return placeTag.tag.in(tags);
    }

    private BooleanExpression placeTagCountEq(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return placeTag.tag.countDistinct().eq((long) tags.size());
    }
}

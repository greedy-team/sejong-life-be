import http from 'k6/http';
import { sleep } from 'k6';

const BASE_URL = 'https://api.sejonglife.site';

const TEST_PLACE_ID = 1;

export const options = {
    scenarios: {
        get_categories:       { exec: 'getCategories',      executor: 'constant-vus', vus: 50, duration: '60s' },
        get_places:           { exec: 'getPlaces',          executor: 'constant-vus', vus: 50, duration: '60s', startTime: '1m6s' },
        get_place_detail:     { exec: 'getPlaceDetail',     executor: 'constant-vus', vus: 50, duration: '60s', startTime: '2m12s' },
        get_hot_places:       { exec: 'getHotPlaces',       executor: 'constant-vus', vus: 50, duration: '60s', startTime: '3m18s' },
        get_tags:             { exec: 'getTags',            executor: 'constant-vus', vus: 50, duration: '60s', startTime: '4m24s' },
        get_recommended_tags: { exec: 'getRecommendedTags', executor: 'constant-vus', vus: 50, duration: '60s', startTime: '5m36s' },
        get_reviews:          { exec: 'getReviews',         executor: 'constant-vus', vus: 50, duration: '60s', startTime: '6m48s' },
        get_review_summary:   { exec: 'getReviewSummary',   executor: 'constant-vus', vus: 50, duration: '60s', startTime: '8m' },
    },
};

export function getCategories() {
    http.get(`${BASE_URL}/api/categories`, { tags: { api: 'get_categories' } });
}

export function getPlaces() {
    const encodedCategory = encodeURIComponent('전체');
    const url = `${BASE_URL}/api/places?category=${encodedCategory}`;
    http.get(url, { tags: { api: 'get_places' } });
}

export function getPlaceDetail() {
    http.get(`${BASE_URL}/api/places/${TEST_PLACE_ID}`, { tags: { api: 'get_place_detail' } });
}

export function getHotPlaces() {
    http.get(`${BASE_URL}/api/places/hot`, { tags: { api: 'get_hot_places' } });
}

export function getTags() {
    http.get(`${BASE_URL}/api/tags`, { tags: { api: 'get_tags' } });
}

export function getRecommendedTags() {
    http.get(`${BASE_URL}/api/tags/recommended?categoryId=1`, { tags: { api: 'get_recommended_tags' } });
}

export function getReviews() {
    http.get(`${BASE_URL}/api/places/${TEST_PLACE_ID}/reviews`, { tags: { api: 'get_reviews' } });
}

export function getReviewSummary() {
    http.get(`${BASE_URL}/api/places/${TEST_PLACE_ID}/reviews/summary`, { tags: { api: 'get_review_summary' } });
}

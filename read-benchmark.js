import http from 'k6/http';
import { Trend } from 'k6/metrics';

const BASE_URL = 'https://api.sejonglife.site';
const TEST_PLACE_ID = 1;

let getCategoriesLatency = new Trend('get_categories_latency');
let getPlacesLatency = new Trend('get_places_latency');
let getPlaceDetailLatency = new Trend('get_place_detail_latency');
let getHotPlacesLatency = new Trend('get_hot_places_latency');
let getTagsLatency = new Trend('get_tags_latency');
let getRecommendedTagsLatency = new Trend('get_recommended_tags_latency');
let getReviewsLatency = new Trend('get_reviews_latency');
let getReviewSummaryLatency = new Trend('get_review_summary_latency');

export const options = {
    scenarios: {
        get_categories:       { exec: 'getCategories',       executor: 'constant-vus', vus: 100, duration: '60s' },
        get_places:           { exec: 'getPlaces',           executor: 'constant-vus', vus: 100, duration: '60s', startTime: '1m' },
        get_place_detail:     { exec: 'getPlaceDetail',      executor: 'constant-vus', vus: 100, duration: '60s', startTime: '2m' },
        get_hot_places:       { exec: 'getHotPlaces',        executor: 'constant-vus', vus: 100, duration: '60s', startTime: '3m' },
        get_tags:             { exec: 'getTags',             executor: 'constant-vus', vus: 100, duration: '60s', startTime: '4m' },
        get_recommended_tags: { exec: 'getRecommendedTags',  executor: 'constant-vus', vus: 100, duration: '60s', startTime: '5m' },
        get_reviews:          { exec: 'getReviews',          executor: 'constant-vus', vus: 100, duration: '60s', startTime: '6m' },
        get_review_summary:   { exec: 'getReviewSummary',    executor: 'constant-vus', vus: 100, duration: '60s', startTime: '7m' },
    },
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)'],
};

function recordLatency(res, latencyTrend) {
    latencyTrend.add(res.timings.duration);
}

export function getCategories() {
    let res = http.get(`${BASE_URL}/api/categories`);
    recordLatency(res, getCategoriesLatency);
}

export function getPlaces() {
    let res = http.get(`${BASE_URL}/api/places?category=${encodeURIComponent('전체')}`);
    recordLatency(res, getPlacesLatency);
}

export function getPlaceDetail() {
    let res = http.get(`${BASE_URL}/api/places/${TEST_PLACE_ID}`);
    recordLatency(res, getPlaceDetailLatency);
}

export function getHotPlaces() {
    let res = http.get(`${BASE_URL}/api/places/hot`);
    recordLatency(res, getHotPlacesLatency);
}

export function getTags() {
    let res = http.get(`${BASE_URL}/api/tags`);
    recordLatency(res, getTagsLatency);
}

export function getRecommendedTags() {
    let res = http.get(`${BASE_URL}/api/tags/recommended?categoryId=1`);
    recordLatency(res, getRecommendedTagsLatency);
}

export function getReviews() {
    let res = http.get(`${BASE_URL}/api/places/${TEST_PLACE_ID}/reviews`);
    recordLatency(res, getReviewsLatency);
}

export function getReviewSummary() {
    let res = http.get(`${BASE_URL}/api/places/${TEST_PLACE_ID}/reviews/summary`);
    recordLatency(res, getReviewSummaryLatency);
}

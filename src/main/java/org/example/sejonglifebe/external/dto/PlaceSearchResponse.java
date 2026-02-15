package org.example.sejonglifebe.external.dto;

public record PlaceSearchResponse(String id,
                                  String name,
                                  String address,
                                  Double latitude,
                                  Double longitude) {}

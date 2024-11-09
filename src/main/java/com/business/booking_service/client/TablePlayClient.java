//package com.business.booking_service.client;
//
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestParam;
//
//import java.util.Map;
//
//@FeignClient(name = "table-service", url = "http://table-service")
//public interface TablePlayClient {
//    @PutMapping("/api/tables/update/{tableId}/status")
//    ResponseEntity<?> updateTableStatus(@PathVariable("tableId") Integer tableId, @RequestBody Map<String, String> request);
////    @PutMapping("/update/{tableId}/status")
////    ResponseEntity<String> updateTableStatus(@PathVariable("tableId") Integer tableId,
////                                             @RequestParam("table_status") String tableStatus);
//}

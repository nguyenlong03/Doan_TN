package poly.store.rest.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import poly.store.entity.Product;
import poly.store.model.StatisticalProductDay;
import poly.store.model.StatisticalRevenue;
import poly.store.model.StatisticalTotalOrder;
import poly.store.service.OrderService;

@CrossOrigin("*")
@RestController
@RequestMapping("/rest")
public class StatisticalRestController {
	@Autowired
	OrderService orderService;
	
	@GetMapping("/statistical/product/day")
	public List<StatisticalProductDay> productDay(){
		return orderService.listStatisticalProductDay();
	}
	
	@GetMapping("/statistical/product/warehouse")
	public List<Product> warehouse(){
		return orderService.listStatisticalProductWarehouse();
	}
	
	@GetMapping("/statistical/revenue/day/{month}/{year}")
	public List<StatisticalRevenue> listRevenueByDay(@PathVariable("month") int month, @PathVariable("year") int year){
		return orderService.listStatisticalRevenue(month, year);
	}
	
	@GetMapping("/statistical/revenue/month/{year}")
	public List<StatisticalRevenue> listRevenueByMonth(@PathVariable("year") int year){
		return orderService.listStatisticalRevenueByMonth(year);
	}
	
	@GetMapping("/statistical/revenue/year/{year}")
	public List<StatisticalRevenue> listRevenueByYear(@PathVariable("year") int year){
		return orderService.listStatisticalRevenueByYear(year);
	}
	
	@GetMapping("/statistical/order/day/{day}/{month}/{year}")
	public StatisticalTotalOrder listOrderByDay(@PathVariable("day") int day, @PathVariable("month") int month, @PathVariable("year") int year){
		return orderService.getStatisticalTotalOrderOnDay(day, month, year);
	}
	
	@GetMapping("/statistical/order/month/{month}/{year}")
	public StatisticalTotalOrder listOrderByMonth(@PathVariable("month") int month, @PathVariable("year") int year){
		return orderService.getStatisticalTotalOrderOnMonth(month, year);
	}
	
	@GetMapping("/statistical/order/year/{year}")
	public StatisticalTotalOrder listOrderByYear(@PathVariable("year") int year){
		return orderService.getStatisticalTotalOrderOnYear(year);
	}
	
	@GetMapping("/statisticalOrder/year")
	public List<Integer> listYear(){
		return orderService.getListYearOrder();
	}
	
	@GetMapping("/statistical/order/option/{day}/{month}/{year}")
	public StatisticalTotalOrder listOrderByOption(@PathVariable("day") int day, @PathVariable("month") int month, @PathVariable("year") int year){
		return orderService.getStatisticalTotalOrderOnOption(day, month, year);
	}

	@GetMapping("/statistical/revenue/export/day/{month}/{year}")
	public void exportRevenueByDay(@PathVariable("month") int month, @PathVariable("year") int year, HttpServletResponse response) throws IOException {
		List<StatisticalRevenue> data = orderService.listStatisticalRevenue(month, year);
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet("Revenue-Day");
		int rowIdx = 0;
		Row header = sheet.createRow(rowIdx++);
		header.createCell(0).setCellValue("Ngày");
		header.createCell(1).setCellValue("Doanh thu (VND)");
		for (StatisticalRevenue r : data) {
			Row row = sheet.createRow(rowIdx++);
			row.createCell(0).setCellValue(r.getDate());
			row.createCell(1).setCellValue(r.getPrice());
		}
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader("Content-Disposition", "attachment; filename=doanh-thu-ngay-"+month+"-"+year+".xlsx");
		wb.write(response.getOutputStream());
		wb.close();
	}

	@GetMapping("/statistical/revenue/export/month/{year}")
	public void exportRevenueByMonth(@PathVariable("year") int year, HttpServletResponse response) throws IOException {
		List<StatisticalRevenue> data = orderService.listStatisticalRevenueByMonth(year);
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet("Revenue-Month");
		int rowIdx = 0;
		Row header = sheet.createRow(rowIdx++);
		header.createCell(0).setCellValue("Tháng");
		header.createCell(1).setCellValue("Doanh thu (VND)");
		int month = 1;
		for (StatisticalRevenue r : data) {
			Row row = sheet.createRow(rowIdx++);
			row.createCell(0).setCellValue(month++);
			row.createCell(1).setCellValue(r.getPrice());
		}
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader("Content-Disposition", "attachment; filename=doanh-thu-thang-"+year+".xlsx");
		wb.write(response.getOutputStream());
		wb.close();
	}

	@GetMapping("/statistical/revenue/export/year/{year}")
	public void exportRevenueByYear(@PathVariable("year") int year, HttpServletResponse response) throws IOException {
		List<StatisticalRevenue> data = orderService.listStatisticalRevenueByYear(year);
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet("Revenue-Year");
		int rowIdx = 0;
		Row header = sheet.createRow(rowIdx++);
		header.createCell(0).setCellValue("Năm");
		header.createCell(1).setCellValue("Doanh thu (VND)");
		for (StatisticalRevenue r : data) {
			Row row = sheet.createRow(rowIdx++);
			row.createCell(0).setCellValue(r.getDate());
			row.createCell(1).setCellValue(r.getPrice());
		}
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader("Content-Disposition", "attachment; filename=doanh-thu-nam-den-"+year+".xlsx");
		wb.write(response.getOutputStream());
		wb.close();
	}
}

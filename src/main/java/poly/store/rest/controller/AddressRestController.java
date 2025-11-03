package poly.store.rest.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import poly.store.entity.District;
import poly.store.entity.Province;
import poly.store.entity.Ward;
import poly.store.model.AddressModel;
import poly.store.service.AddressService;

/**
 * Class cung cap cac dich vu rest api cho bang employee
 * 
 * @author khoa-ph
 * @version 1.00
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/rest")
public class AddressRestController {
	private static final Logger logger = LoggerFactory.getLogger(AddressRestController.class);

	@Autowired
	AddressService addressService;
	
	@GetMapping("/province")
	public ResponseEntity<List<Province>> list(){
		try {
			logger.info("GET /rest/province - Fetching all provinces");
			List<Province> provinces = addressService.findAllProvince();
			logger.info("Returning " + provinces.size() + " provinces");
			return ResponseEntity.ok(provinces);
		} catch (Exception e) {
			logger.error("Error in /rest/province: " + e.getMessage(), e);
			return ResponseEntity.ok(new ArrayList<>());
		}
	}
	
	@GetMapping("/district/{id}")
	public ResponseEntity<List<District>> listDistrict(@PathVariable("id") Integer id){
		try {
			logger.info("GET /rest/district/" + id + " - Fetching districts");
			List<District> districts = addressService.findDistrictByIdProvince(id);
			logger.info("Returning " + districts.size() + " districts");
			return ResponseEntity.ok(districts);
		} catch (Exception e) {
			logger.error("Error in /rest/district/" + id + ": " + e.getMessage(), e);
			return ResponseEntity.ok(new ArrayList<>());
		}
	}
	
	@GetMapping("/ward/{idProvince}/{idDistrict}")
	public ResponseEntity<List<Ward>> listWard(@PathVariable("idProvince") Integer idProvince, @PathVariable("idDistrict") Integer idDistrict){
		try {
			logger.info("GET /rest/ward/" + idProvince + "/" + idDistrict + " - Fetching wards");
			List<Ward> wards = addressService.findWardByIdProvinceAndIdDistrict(idProvince, idDistrict);
			logger.info("Returning " + wards.size() + " wards");
			return ResponseEntity.ok(wards);
		} catch (Exception e) {
			logger.error("Error in /rest/ward/" + idProvince + "/" + idDistrict + ": " + e.getMessage(), e);
			return ResponseEntity.ok(new ArrayList<>());
		}
	}
	
	@PostMapping("/address/form")
	public AddressModel create(@RequestBody AddressModel addressModel) {
		return addressService.createAddress(addressModel);
	}
	
	@GetMapping("/address/update/{id}")
	public AddressModel getOneAddressById(@PathVariable("id") int id) {
		return addressService.getOneAddressById(id);
	}
	
	@GetMapping("/update/district/{id}")
	public List<District> getListDistrictById(@PathVariable("id") Integer id){
		return addressService.getListDistrictByAdressId(id);
	}
	
	@GetMapping("/update/ward/{id}")
	public List<Ward> getListWardById(@PathVariable("id") Integer id){
		return addressService.getListWardByAdressId(id);
	}
	
	@PutMapping("/address/form/{id}")
	public AddressModel update(@PathVariable("id") Integer id, @RequestBody AddressModel addressModel) {
		return addressService.updateAddress(addressModel, id);
	}
}

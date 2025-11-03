package poly.store.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.MapperFeature;

import poly.store.dao.AddressDao;
import poly.store.dao.ProvinceDao;
import poly.store.dao.UserDao;
import poly.store.entity.Address;
import poly.store.entity.District;
import poly.store.entity.Province;
import poly.store.entity.User;
import poly.store.entity.Ward;
import poly.store.model.AddressModel;
import poly.store.service.AddressService;

@Service
@Repository
public class AddressServiceImpl implements AddressService{
	private static final Logger logger = LoggerFactory.getLogger(AddressServiceImpl.class);

	@Autowired
	AddressDao addressDao;
	
	@Autowired
	UserDao userDao;
	
	@Override
	public List<Address> findListAddressByEmail(String username) {
		return addressDao.findListAddressByEmail(username);
	}

	RestTemplate rest = new RestTemplate();
	String url = "https://addressapi-812db-default-rtdb.firebaseio.com/.json";

	private ProvinceDao loadFromClasspath() {
        try {
            ClassPathResource resource = new ClassPathResource("static/assets/json/local.json");
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
            List<Province> provinces = mapper.readValue(resource.getInputStream(), new TypeReference<List<Province>>() {});
            ProvinceDao dao = new ProvinceDao();
            if (provinces != null) dao.addAll(provinces);
            logger.info("Loaded provinces from classpath local.json: {}", dao.size());
            return dao;
        } catch (Exception e) {
            logger.error("Failed to load local provinces from classpath: " + e.getMessage(), e);
            return new ProvinceDao();
        }
    }

	private ProvinceDao getProvinceData() {
		try {
			logger.info("Fetching provinces from Firebase: " + url);
			ProvinceDao list = rest.getForObject(url, ProvinceDao.class);
			if (list != null && !list.isEmpty()) {
				logger.info("Successfully fetched {} provinces from Firebase", list.size());
				return list;
			}
		} catch (Exception e) {
			logger.warn("Failed to fetch from Firebase: {}", e.getMessage());
		}

		// Fallback: read from bundled classpath JSON (offline-safe)
		ProvinceDao local = loadFromClasspath();
		if (!local.isEmpty()) return local;

		logger.error("All data sources failed, returning empty list");
		return new ProvinceDao();
	}

	@Override
	public List<Province> findAllProvince() {
		try {
			logger.info("Fetching provinces");
			ProvinceDao list = getProvinceData();
			if (list == null || list.isEmpty()) {
				logger.warn("Province list is empty or null");
				return new ArrayList<>();
			}
			logger.info("Successfully fetched {} provinces", list.size());
			return list;
		} catch (Exception e) {
			logger.error("Error fetching provinces: " + e.getMessage(), e);
			return new ArrayList<>();
		}
	}

	@Override
	public List<District> findDistrictByIdProvince(Integer id) {
		try {
			logger.info("Fetching districts for province id: " + id);
			ProvinceDao list = getProvinceData();
			if (list == null || list.isEmpty() || id >= list.size()) {
				logger.warn("Invalid province id or empty list");
				return new ArrayList<>();
			}
			List<District> listDistrict = list.get(id).getDistricts();
			logger.info("Successfully fetched " + (listDistrict != null ? listDistrict.size() : 0) + " districts");
			return listDistrict != null ? listDistrict : new ArrayList<>();
		} catch (Exception e) {
			logger.error("Error fetching districts: " + e.getMessage(), e);
			return new ArrayList<>();
		}
	}

	@Override
	public List<Ward> findWardByIdProvinceAndIdDistrict(Integer idProvince, Integer idDistrict) {
		try {
			logger.info("Fetching wards for province id: " + idProvince + ", district id: " + idDistrict);
			ProvinceDao list = getProvinceData();
			if (list == null || list.isEmpty() || idProvince >= list.size()) {
				logger.warn("Invalid province id or empty list");
				return new ArrayList<>();
			}
			List<District> listDistrict = list.get(idProvince).getDistricts();
			if (listDistrict == null || listDistrict.isEmpty() || idDistrict >= listDistrict.size()) {
				logger.warn("Invalid district id or empty district list");
				return new ArrayList<>();
			}
			List<Ward> listWard = listDistrict.get(idDistrict).getWards();
			logger.info("Successfully fetched " + (listWard != null ? listWard.size() : 0) + " wards");
			return listWard != null ? listWard : new ArrayList<>();
		} catch (Exception e) {
			logger.error("Error fetching wards: " + e.getMessage(), e);
			return new ArrayList<>();
		}
	}

	@Override
	public AddressModel createAddress(AddressModel addressModel) {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = ((UserDetails) principal).getUsername();
		
		User temp = userDao.findUserByEmail(username);
		ProvinceDao list = getProvinceData();
		Province province = list.get(Integer.parseInt(addressModel.getProvince()));
		District district = province.getDistricts().get(Integer.parseInt(addressModel.getDistrict()));
		Ward ward = district.getWards().get(Integer.parseInt(addressModel.getWard()));
		
		Address address = new Address();
		address.setFullname(addressModel.getFullName());
		address.setPhone(addressModel.getPhone());
		address.setDetail(addressModel.getDetail());
		address.setProvince(province.getName());
		address.setDistrict(district.getName());
		address.setWard(ward.getName());
		address.setUser(temp);
		addressDao.save(address);

		addressModel.setId(address.getId());
		return addressModel;
	}

	@Override
	public Address getAddressById(int id) {	
		return addressDao.findById(id).get();
	}

	@Override
	public void delete(Address address) {		
		addressDao.delete(address);
	}

	@Override
	public Address findAddressById(String username, int id) {
		return addressDao.findAddressById(username, id);
	}

	@Override
	public AddressModel getOneAddressById(int id) {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = ((UserDetails) principal).getUsername();
		
		Address address = addressDao.findAddressById(username, id);
		
		AddressModel addressModel = new AddressModel();
		
		addressModel.setFullName(address.getFullname());
		addressModel.setPhone(address.getPhone());
		addressModel.setDetail(address.getDetail());
		
		addressModel.setProvince(address.getProvince());
		addressModel.setDistrict(address.getDistrict());
		addressModel.setWard(address.getWard());
		
		return addressModel;
	}

	@Override
	public List<District> getListDistrictByAdressId(Integer id) {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = ((UserDetails) principal).getUsername();
		
		Address address = addressDao.findAddressById(username, id);
		
		ProvinceDao list = getProvinceData();

		List<District> listDistrict = new ArrayList<>();
		
		for(int i = 0; i<list.size(); i++) {
			if(list.get(i).getName().equals(address.getProvince())) {				
				listDistrict = list.get(i).getDistricts();				
				break;
			}	
		}
		return listDistrict;
	}

	@Override
	public List<Ward> getListWardByAdressId(Integer id) {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = ((UserDetails) principal).getUsername();
		
		Address address = addressDao.findAddressById(username, id);		
		
		ProvinceDao list = getProvinceData();

		List<District> listDistrict = new ArrayList<>();
		List<Ward> listWard = new ArrayList<>();
		
		for(int i = 0; i<list.size(); i++) {
			if(list.get(i).getName().equals(address.getProvince())) {				
				listDistrict = list.get(i).getDistricts();				
				for(int j = 0; j<listDistrict.size(); j++) {
					if(listDistrict.get(j).getName().equals(address.getDistrict())) {
						listWard = listDistrict.get(j).getWards();
						break;
					}
				}
				break;
			}	
		}
		return listWard;
	}

	@Override
	public AddressModel updateAddress(AddressModel addressModel, Integer id) {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = ((UserDetails) principal).getUsername();
		
		ProvinceDao list = getProvinceData();
		Province province = list.get(Integer.parseInt(addressModel.getProvince()));
		District district = province.getDistricts().get(Integer.parseInt(addressModel.getDistrict()));
		Ward ward = district.getWards().get(Integer.parseInt(addressModel.getWard()));
		User temp = userDao.findUserByEmail(username);
		Address address = addressDao.findAddressById(username, id);
		
		address.setFullname(addressModel.getFullName());
		address.setPhone(addressModel.getPhone());
		address.setDetail(addressModel.getDetail());
		address.setProvince(province.getName());
		address.setDistrict(district.getName());
		address.setWard(ward.getName());
		address.setUser(temp);
		addressDao.save(address);
		
		addressModel.setId(address.getId());
		return addressModel;
	}


}

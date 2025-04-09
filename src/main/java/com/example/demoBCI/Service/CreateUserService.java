package com.example.demoBCI.Service;

import com.example.demoBCI.DTO.Request.PhoneRequestDTO;
import com.example.demoBCI.DTO.Request.UserRequestDTO;
import com.example.demoBCI.DTO.Response.PhoneResponseDTO;
import com.example.demoBCI.DTO.Response.UserResponseDTO;
import com.example.demoBCI.Entity.Phone;
import com.example.demoBCI.Entity.User;
import com.example.demoBCI.Exceptions.DemoBCIException;
import com.example.demoBCI.Mapper.PhoneToPhoneDTO;
import com.example.demoBCI.Mapper.UserDTOToUser;
import com.example.demoBCI.Repository.PhoneRepository;
import com.example.demoBCI.Repository.UserRepository;
import com.example.demoBCI.Util.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Service
public class CreateUserService {

    private final UserRepository userRepository;
    private final PhoneRepository phoneRepository;
    private final UserDTOToUser mapperUser;
    private final PhoneToPhoneDTO mapperPhone;

    public CreateUserService(UserRepository userRepository, PhoneRepository phoneRepository, UserDTOToUser mapperUser, PhoneToPhoneDTO mapperPhone, Validator validator) {
        this.userRepository = userRepository;
        this.phoneRepository = phoneRepository;
        this.mapperUser = mapperUser;
        this.mapperPhone = mapperPhone;
    }


    public UserResponseDTO createUser(UserRequestDTO userRequestDTO){
        User user = new User();
        User userPhones = new User();
        boolean emailValidator = Validator.emailValidator(userRequestDTO.getEmail());
        boolean passValidator = Validator.passValidator(userRequestDTO.getPassword());
        User existEmail = this.userRepository.findByEmail(userRequestDTO.getEmail());

        if(!emailValidator){
            throw  new DemoBCIException("email invalido",HttpStatus.NOT_FOUND);
        }else if(!passValidator){
            throw  new DemoBCIException("contraseña invalida",HttpStatus.NOT_FOUND);
        }else if(Objects.nonNull(existEmail)){
            throw  new DemoBCIException("email ya registrado",HttpStatus.NOT_FOUND);
        }else{
                user = mapperUser.map(userRequestDTO);
                this.userRepository.save(user);
                userPhones = this.userRepository.findByUuid(user.getUuid());
                if(userPhones != null ){
                    for(int i=0; i < userRequestDTO.getPhones().size(); i++){
                        createPhone(userPhones, userRequestDTO.getPhones().get(i));
                    }
                }
            }

        return generateResponse(userPhones);
    }

    private Phone createPhone(User user, PhoneRequestDTO phones){
        Phone phoneCreate = new Phone();
        phoneCreate.setNumber(phones.getNumber());
        phoneCreate.setCityCode(phones.getCitycode());
        phoneCreate.setCountryCode(phones.getContrycode());
        phoneCreate.setUuidUser(user.getUuid());
        return phoneRepository.save(phoneCreate);
    }

    private UserResponseDTO generateResponse(User user){
        User getUser = userRepository.findByUuid(user.getUuid());
        List<Phone> phones  = phoneRepository.findByUuidUser(user.getUuid());

        UserResponseDTO userResponse  = new UserResponseDTO();
        userResponse.setName(getUser.getName());
        userResponse.setEmail(getUser.getEmail());
        userResponse.setPassword(getUser.getPassword());
        userResponse.setUuid(getUser.getUuid());
        userResponse.setCreated(getUser.getCreated());
        userResponse.setModified(getUser.getModified());
        userResponse.setLastLogin(getUser.getLastLogin());
        userResponse.setToken(getUser.getToken());
        userResponse.setIsActive(getUser.getIsActive());
        userResponse.setPhones(converterPhones(phones));

        return userResponse;
    }
    private List<PhoneResponseDTO> converterPhones(List<Phone> phones){
        List<PhoneResponseDTO> phonesResponseDTO = new ArrayList<>();
        for (Phone phone : phones) {
            PhoneResponseDTO phoneResponseMapper = mapperPhone.map(phone);
            phonesResponseDTO.add(phoneResponseMapper);
        }
        return phonesResponseDTO;
    }
}

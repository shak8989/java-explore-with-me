
package ru.practicum.ewm.user;


import java.util.Comparator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto createUser(NewUserRequest request) {
        User user = UserMapper.toUser(request);

        User savedUser = userRepository.save(user);

        return UserMapper.toUserDto(savedUser);
    }

    public List<UserDto> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .toList();
    }


    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        List<User> users = ids == null
                ? userRepository.findAll()
                : userRepository.findAllById(ids);

        return users.stream()
                .sorted(Comparator.comparing(User::getId))
                .skip(from)
                .limit(size)
                .map(UserMapper::toUserDto)
                .toList();
    }


    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }


}

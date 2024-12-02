package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //对前端传过来的明文密码进行md5处理
        password=DigestUtils.md5DigestAsHex(password.getBytes());

        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     * @param employeeDTO
     */
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();

        //一个一个属性赋值太麻烦
        //使用对象属性拷贝(属性名必须一致)
        BeanUtils.copyProperties(employeeDTO,employee);

        //设置账号的状态，默认正常状态 1表示正常，0表示锁定
        //不能硬编码，所以使用常量类 TODO
        employee.setStatus(StatusConstant.ENABLE);

        //设置账号的默认密码，默认密码123456
        //注意要md5加密,同样不使用常量类 TODO
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        //设置当前记录的创建时间和修改时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //设置当前记录创建人id和修改人id
        //TODO 后期需要改为当前登录用户的id
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());

        //插入员工数据
        employeeMapper.insert(employee);
    }

    /**
     * 分页查询
     * @param employeePageQueryDTO
     * @return
     */
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        //基于SQL的limit关键字实现分页查询，后面的数字是具体的参数
        // select * from employee limit 0,10

        //开始分页查询
        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());
        Page<Employee> page=employeeMapper.pageQuery(employeePageQueryDTO);

        //加工成期望的返回结果
        long total=page.getTotal();
        List<Employee> records=page.getResult();
        return new PageResult(total,records);
    }

    /**
     * 启用禁用员工账号
     * @param status
     * @param id
     */
    public void enableOrDisable(Integer status, Long id) {
        //最终发一个SQL：
        //update employee set status=? where id=?

        //这里当然可以new一个employee
        //也可以这样做，用builder
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();

        //想做动态更新，更通用的写法
        employeeMapper.update(employee);
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    public Employee getById(Long id) {
        //调用持久层来查询，但是会返回密码，这是我们所不想暴露给用户的
        Employee employee=employeeMapper.getById(id);
        //稍微处理一下密码，调用set方法即可
        employee.setPassword("****");
        return employee;
    }

    /**
     * 编辑员工信息
     * @param employeeDTO
     */
    public void update(EmployeeDTO employeeDTO) {
        //前面的知识
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());

        //前面写好的方法起作用了，别急着高兴，注意下面这个方法传的是employee对象，需要进行属性拷贝
        employeeMapper.update(employee);
    }

    /**
     * 修改密码
     * @param passwordEditDTO
     */
    public void editPassword(PasswordEditDTO passwordEditDTO) {
        //TODO 考虑前端的业务逻辑进行联调测试
        //TODO 逻辑上来说应该加上新旧密码的比较,这两个不能相同;而且要重复两次输入新密码,这两次必须相同
        Employee employee=employeeMapper.getById(passwordEditDTO.getEmpId());
        Employee editEmployee = new Employee();
        String oldPassword = DigestUtils.md5DigestAsHex(passwordEditDTO.getOldPassword().getBytes());
        if (!oldPassword.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }else{
            //密码正确,注意新密码的MD5处理
            editEmployee.setId(passwordEditDTO.getEmpId());
            String newPassword=DigestUtils.md5DigestAsHex(passwordEditDTO.getNewPassword().getBytes());

            //修改密码
            editEmployee.setPassword(newPassword);
            editEmployee.setUpdateTime(LocalDateTime.now());
            editEmployee.setUpdateUser(BaseContext.getCurrentId());
            //传editEmployee对象
            employeeMapper.update(editEmployee);
        }
    }

}

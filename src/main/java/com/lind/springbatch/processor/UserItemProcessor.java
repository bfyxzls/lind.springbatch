package com.lind.springbatch.processor;

import com.lind.springbatch.entity.User;
import javax.validation.ValidationException;
import org.springframework.batch.item.validator.ValidatingItemProcessor;

public class UserItemProcessor extends ValidatingItemProcessor<User> {
  @Override
  public User process(User item) throws ValidationException {
    super.process(item);
    item.setName(item.getName() + "_china");
    return item;
  }
}

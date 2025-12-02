import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { User } from './user.entity';
import { Repository } from 'typeorm';

@Injectable()
export class UserService {
  constructor(
    @InjectRepository(User)
    private userRepository: Repository<User>,
  ) {}

  public findOneById(id: number) {
    return this.userRepository.findOneBy({ id });
  }

  public async findAll() {
    return this.userRepository.find();
  }

  public create(data: Partial<User>) {
    const user = this.userRepository.create(data);
    return this.userRepository.save(user);
  }

  public async remove(id: number) {
    await this.userRepository.delete(id);
    return { deleted: true };
  }
}

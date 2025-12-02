import { Column, Entity, PrimaryGeneratedColumn } from 'typeorm';

@Entity()
export class User {
  @PrimaryGeneratedColumn()
  public id: number;

  @Column()
  public login: string;

  @Column()
  public name: string;

  @Column()
  public password: string;

  constructor(login: string, name: string, password: string) {
    this.login = login;
    this.name = name;
    this.password = password;
  }
}

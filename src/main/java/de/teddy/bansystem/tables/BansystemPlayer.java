package de.teddy.bansystem.database.tables;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.sql.Date;
import java.util.Objects;

@Getter
@Setter
@ToString
@Entity
@Table(name = "bansystem_player")
public class BansystemPlayer implements Serializable {

	@Serial private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "uuid", nullable = false, unique = true, length = 36, columnDefinition = "varchar(36)")
	private String uuid;

	@Column(name = "username")
	private String username;

	@Column(name = "first_login")
	private Date firstLogin;

	@Column(name = "last_login")
	private Date lastLogin;

	public BansystemPlayer(){}

	public BansystemPlayer(String uuid, Date firstLogin){
		this.uuid = uuid;
		this.firstLogin = firstLogin;
	}

	public String getUuid(){
		return uuid;
	}

	public void setUuid(String uuid){
		this.uuid = uuid;
	}

	public String getUsername(){
		return username;
	}

	public void setUsername(String username){
		this.username = username;
	}

	public Date getFirstLogin(){
		return firstLogin;
	}

	public void setFirstLogin(Date firstLogin){
		this.firstLogin = firstLogin;
	}

	public Date getLastLogin(){
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin){
		this.lastLogin = lastLogin;
	}


	@Override
	public boolean equals(Object o){
		if(this == o)
			return true;
		if(o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
			return false;
		BansystemPlayer that = (BansystemPlayer)o;
		return uuid != null && Objects.equals(uuid, that.uuid);
	}

	@Override
	public int hashCode(){
		return getClass().hashCode();
	}
}

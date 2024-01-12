package de.teddy.bansystem.database.tables;

import org.hibernate.Hibernate;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "bansystem_whitelist")
public class BansystemWhitelist implements Serializable {

	@Serial private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "whitelist_id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer whitelistId;

	public void setBansystemPlayer(BansystemPlayer bansystemPlayer){
		this.bansystemPlayer = bansystemPlayer;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "player_id")
	@NotFound(action = NotFoundAction.IGNORE)
	private BansystemPlayer bansystemPlayer;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "token_id")
	BansystemToken bansystemToken;

	public BansystemPlayer getPlayer(){return bansystemPlayer;}

	public Integer getWhitelistId(){return whitelistId;}

	public BansystemToken getBansystemToken(){
		return bansystemToken;
	}

	public void setBansystemToken(BansystemToken bansystemToken){
		this.bansystemToken = bansystemToken;
	}

	@Override
	public boolean equals(Object o){
		if(this == o)
			return true;
		if(o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
			return false;
		BansystemWhitelist that = (BansystemWhitelist)o;
		return whitelistId != null && Objects.equals(whitelistId, that.whitelistId);
	}

	@Override
	public int hashCode(){
		return getClass().hashCode();
	}
}

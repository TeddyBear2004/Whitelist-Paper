package de.teddy.bansystem.tables;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import org.hibernate.query.Query;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@ToString
@Entity
@Table(name = "bansystem_whitelist")
public class BansystemWhitelist implements Serializable {

	@Serial private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "whitelist_id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer whitelistId;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "player_id")
	@NotFound(action = NotFoundAction.IGNORE)
	private BansystemPlayer player;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "token_id")
	BansystemToken bansystemToken;

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

	public static Query<BansystemWhitelist> whitelistQuery(Session session, UUID uuid) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<BansystemWhitelist> query = builder.createQuery(BansystemWhitelist.class);
		Root<BansystemWhitelist> root = query.from(BansystemWhitelist.class);
		query.select(root).where(builder.equal(root.get("player").get("uuid"), uuid.toString()));
		return session.createQuery(query);
	}

	public static Query<BansystemWhitelist> whitelistQuery(Session session, UUID uuid, String gamemode) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<BansystemWhitelist> query = builder.createQuery(BansystemWhitelist.class);
		Root<BansystemWhitelist> root = query.from(BansystemWhitelist.class);
		query.select(root).where(builder.and(
				builder.equal(root.get("player").get("uuid"), uuid.toString()),
				builder.equal(root.get("bansystemToken").get("gamemode"), gamemode)
		));
		return session.createQuery(query);
	}
}
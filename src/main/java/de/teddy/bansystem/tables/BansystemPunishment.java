package de.teddy.bansystem.tables;

import de.teddy.util.TimeUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "bansystem_punishments")
public class BansystemPunishment implements Serializable {

	@Serial private static final long serialVersionUID = -12525865;

	@Id
	@Column(name = "punishment_id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer punishmentId;

	@Setter
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "player_id")
	private BansystemPlayer player;

	@Setter
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "staff_id")
	private BansystemPlayer staff;

	@Column(name = "start_time")
	private long startTime;

	@Setter
	@Column(name = "duration")
	private long duration;

	@Setter
	@Column(name = "active")
	private boolean active;

	@Setter
	@Column(name = "type", nullable = false)
	private String type;

	@Column(name = "reason")
	private String reason;

	public BansystemPunishment(){}

	public BansystemPunishment(BansystemPlayer player, BansystemPlayer staff, long startTime, long duration, String type, String reason){
		this.player = player;
		this.staff = staff;
		this.startTime = startTime;
		this.duration = duration;
		this.type = type;
		this.reason = reason;
	}

	public String getBanScreenMessage(){
		return ChatColor.DARK_RED + "Du wurdest von diesem Netzwerk gebannt.\n" +
				"\n" +
				ChatColor.YELLOW + "Grund: " +
				ChatColor.RED + getReason() + "\n\n" +
				ChatColor.YELLOW + "Verbleibende Zeit: " +
				ChatColor.RED + (getDuration() < 0 ? "PERMANENT" : TimeUtil.parseMillis((getStartTime() + getDuration() - System.currentTimeMillis())));
	}

	@Override
	public boolean equals(Object o){
		if(this == o)
			return true;
		if(o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
			return false;
		BansystemPunishment that = (BansystemPunishment)o;
		return punishmentId != null && Objects.equals(punishmentId, that.punishmentId);
	}

	@Override
	public int hashCode(){
		return getClass().hashCode();
	}
}

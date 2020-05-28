package org.kepr.hostapi.data

import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import javax.persistence.*
import javax.validation.constraints.NotBlank

@Entity(name="alias")
data class Alias(@Column(unique=true) @NotBlank var name: String, @OneToMany(mappedBy = "alias", fetch = FetchType.LAZY) @Cascade(CascadeType.PERSIST, CascadeType.MERGE)
var hosts: MutableList<Host>) {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null

    override fun toString(): String {
       return "name: ${this.name}}"
    }
}

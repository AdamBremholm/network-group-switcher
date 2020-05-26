package org.kepr.hostapi.data

import javax.persistence.*
import javax.validation.constraints.NotBlank

@Entity(name="alias")
data class Alias(@Column(unique=true) @NotBlank val name: String, @OneToMany(cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY)
var hosts: MutableList<Host>) {

    @Id @GeneratedValue val id: Long? = null


}

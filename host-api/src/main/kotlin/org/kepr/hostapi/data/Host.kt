package org.kepr.hostapi.data

import javax.persistence.*
import javax.validation.constraints.Size

@Entity(name="host")
data class Host(@Column(unique=true) @Size(min=1)val address: String, @Column(unique=true) @Size(min=1)val name: String, @ManyToOne(cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY)
var alias: Alias? = null) {

    @Id @GeneratedValue val id: Long? = null

}
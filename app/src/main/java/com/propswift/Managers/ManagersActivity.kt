package com.propswift.Managers

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.propswift.R
import com.propswift.Shared.dismissProgress
import com.propswift.Shared.goToActivity_Unfinished
import com.propswift.Shared.myViewModel
import com.propswift.Shared.settingsClick
import com.propswift.databinding.ActivityManagersBinding
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import kotlinx.coroutines.*

class ManagersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManagersBinding
    var propertyid = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initall()
    }

    private fun initall() {

        settingsClick(binding.include.menuicon)

        val layoutManager = LinearLayoutManager(this)
        lateinit var expensesAdapter: ManagersAdapter
        binding.managersRecyclerView.setLayoutManager(layoutManager)

        binding.include.header.setText("Managers")
        binding.include.mainTabs.visibility = View.GONE

        binding.addManager.setOnClickListener {
            goToActivity_Unfinished(this@ManagersActivity, AddManagerActivity::class.java)
        }

        binding.chooseproperty.setOnClickListener {

            CoroutineScope(Dispatchers.IO).launch() {

                val listOfOwnedProperties = async { myViewModel(this@ManagersActivity).getOwnedproperties() }
                val thelist = runBlocking { listOfOwnedProperties.await() }
                Log.d("-------", "initall: FOUND THE LIST TO BE ${thelist.toString()}")


                val powerMenu: PowerMenu.Builder? = PowerMenu.Builder(this@ManagersActivity).setAnimation(MenuAnimation.SHOWUP_TOP_LEFT) // Animation start point (TOP | LEFT).
                    .setMenuRadius(10f) // sets the corner radius.
                    .setMenuShadow(10f) // sets the shadow.
                    .setWidth(900).setTextColor(ContextCompat.getColor(this@ManagersActivity, R.color.black)).setTextGravity(Gravity.CENTER)
                    .setTextTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD)).setSelectedTextColor(Color.WHITE).setMenuColor(Color.WHITE)
                    .setSelectedMenuColor(ContextCompat.getColor(this@ManagersActivity, R.color.colorPrimary)).setAutoDismiss(true)

                withContext(GlobalScope.coroutineContext) {
                    thelist.details.let {
                        it?.forEach {
                            powerMenu?.addItem(PowerMenuItem(it.name))
                            Log.d("-------", "initall: FOUND ITEM ${it.name}")
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    powerMenu?.setOnMenuItemClickListener { position, item ->
                        val chosenposition = position
                        val propertyname = item.title.toString()
                        propertyid = thelist.details?.get(chosenposition)!!.id.toString()
                        binding.chooseproperty.setText(propertyname)

                        CoroutineScope(Dispatchers.IO).launch() {
                            val allpropertyForManagers = myViewModel(this@ManagersActivity).getPropertyManagers(propertyid)
                            withContext(Dispatchers.Main) {
                                expensesAdapter = ManagersAdapter(this@ManagersActivity, allpropertyForManagers, propertyid)
                                binding.managersRecyclerView.setAdapter(expensesAdapter)
                                expensesAdapter.notifyDataSetChanged()
                                dismissProgress()
                            }
                        }

                    }

                    powerMenu?.build()?.showAsDropDown(binding.chooseproperty)

                }

            }
        }


    }
}
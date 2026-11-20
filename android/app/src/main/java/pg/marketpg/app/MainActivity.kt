package pg.marketpg.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Lime = Color(0xFFB7F34A)
private data class Product(val title:String,val price:String,val location:String,val category:String)
private val demo = listOf(
 Product("iPhone 13 • 128GB","K 1,650","Lae","Phones"),
 Product("Samsung Smart TV 43”","K 1,200","Port Moresby","Electronics"),
 Product("Office study desk","K 350","Goroka","Furniture"),
 Product("Mountain bike","K 480","Lae","Sports"),
 Product("PlayStation 5","K 2,100","Port Moresby","Gaming"),
 Product("Dining set • 6 chairs","K 900","Madang","Home")
)

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { MarketPGApp() } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketPGApp() {
 var search by remember { mutableStateOf("") }
 var selected by remember { mutableStateOf("All") }
 var tab by remember { mutableIntStateOf(0) }
 val categories = listOf("All","Phones","Electronics","Furniture","Vehicles","Fashion")
 val products = demo.filter { (selected=="All" || it.category==selected) && it.title.contains(search,true) }
 MaterialTheme(colorScheme = lightColorScheme(primary=Color(0xFF26321B), secondary=Lime, background=Color(0xFFF7F8F4))) {
  Scaffold(containerColor=Color(0xFFF7F8F4), bottomBar={
   NavigationBar(containerColor=Color.White) {
    listOf("Explore","Saved","Sell","Inbox","Profile").forEachIndexed { i,label ->
     NavigationBarItem(selected=tab==i,onClick={tab=i},icon={Text(listOf("⌂","♡","+","✉","☺")[i],fontSize=20.sp)},label={Text(label)})
    }
   }
  }, floatingActionButton={ if(tab==2) ExtendedFloatingActionButton(onClick={},containerColor=Lime,contentColor=Color.Black,icon={Icon(Icons.Default.Add,null)},text={Text("Create listing")}) }) { padding ->
   Column(Modifier.fillMaxSize().padding(padding)) {
    Row(Modifier.fillMaxWidth().padding(horizontal=20.dp, vertical=16.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
     Column { Text("MarketPG",fontSize=26.sp,fontWeight=FontWeight.Black,color=Color(0xFF26321B)); Text("Buy & sell across PNG",fontSize=12.sp,color=Color.Gray) }
     Text("🇵🇬",fontSize=26.sp)
    }
    OutlinedTextField(value=search,onValueChange={search=it},modifier=Modifier.fillMaxWidth().padding(horizontal=16.dp),placeholder={Text("Search phones, cars, furniture...")},leadingIcon={Icon(Icons.Default.Search,null)},singleLine=true,shape=RoundedCornerShape(16.dp))
    Card(Modifier.fillMaxWidth().padding(16.dp),colors=CardDefaults.cardColors(containerColor=Color(0xFF26321B)),shape=RoundedCornerShape(22.dp)) {
     Column(Modifier.padding(20.dp)) { Text("GREAT LOCAL FINDS",color=Lime,fontWeight=FontWeight.Bold,fontSize=11.sp); Text("Give good things\na second life.",color=Color.White,fontSize=27.sp,fontWeight=FontWeight.ExtraBold); Text("Discover deals from sellers near you.",color=Color.LightGray); Spacer(Modifier.height(10.dp)); Button(onClick={tab=2},colors=ButtonDefaults.buttonColors(containerColor=Lime,contentColor=Color.Black)){Text("Start selling")}}
    }
    Text("Browse categories",Modifier.padding(start=18.dp),fontWeight=FontWeight.Bold,fontSize=18.sp)
    Row(Modifier.fillMaxWidth().padding(12.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
     categories.take(4).forEach { c -> FilterChip(selected=selected==c,onClick={selected=c},label={Text(c)}) }
    }
    Row(Modifier.fillMaxWidth().padding(horizontal=18.dp),horizontalArrangement=Arrangement.SpaceBetween){Text("Fresh finds",fontSize=19.sp,fontWeight=FontWeight.Bold); Text("See all",color=Color(0xFF557A20))}
    LazyVerticalGrid(columns=GridCells.Fixed(2),contentPadding=PaddingValues(12.dp),horizontalArrangement=Arrangement.spacedBy(10.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
     items(products) { p ->
      Card(shape=RoundedCornerShape(16.dp),colors=CardDefaults.cardColors(containerColor=Color.White),modifier=Modifier.clickable { }) {
       Column {
        Box(Modifier.fillMaxWidth().height(112.dp).background(Color(0xFFE9EDDF)),contentAlignment=Alignment.Center){Text("📦",fontSize=38.sp)}
        Column(Modifier.padding(11.dp)) { Text(p.title,fontWeight=FontWeight.SemiBold,maxLines=2,minLines=2); Text(p.price,color=Color(0xFF547C1D),fontWeight=FontWeight.ExtraBold,fontSize=17.sp); Text("⌖ ${p.location}",color=Color.Gray,fontSize=11.sp) }
       }
      }
     }
    }
   }
  }
 }
}
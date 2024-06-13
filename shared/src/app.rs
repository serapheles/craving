use android_logger::Config;
pub use crux_core::App;
use crux_core::render::Render;
use crux_http::Http;
use crux_kv::KeyValue;
use log::LevelFilter;
use serde::{Deserialize, Serialize};

//I don't want to share a personal IP, so this lets me hard code it in a way I can push to GitHub.
//(Adds the file at compile time, which is a kinda fun trick.)
const API_IP: &[u8] = include_bytes!("../res/api.ip");

//Struct for items found in the api response.
//Directly copied from what is used server side in the interest of time.
#[derive(Debug, Serialize, Deserialize, Default, Clone, PartialEq, Eq)]
pub struct Item {
    item_name: String,
    ingredients: Vec<String>,
    updated: String,
    price: String,
    restaurant: String,
}

//Sets up a hook to log in Android studio.
//Would probably log on a live device if I set up a log file.
fn native_activity_create() {
    //Ostensibly this line can be used to only set up logging on Android, if this was being used
    //on multiple platforms.
    // #[cfg(target_os = "android")]
    android_logger::init_once(Config::default().with_max_level(LevelFilter::Trace));
}

//Only a handful of events because we're really just making a single call to an API.
#[derive(Debug, Serialize, Deserialize, PartialEq, Eq)]
pub enum Event {
    Initialize,
    Search,
    SetSearch(String),
    #[serde(skip)]
    SetSearchResult(crux_http::Result<crux_http::Response<Vec<Item>>>),
}

//Core model
//Very bare.
#[derive(Serialize, Deserialize, Default)]
pub struct Model {
    api_endpoint: String,
    search_term: String,
    search_result: Option<Vec<Item>>,
}

//View model. Considering how basic the app is, there's a lot of overlap with the Core model, and
//they could feasibly be consolidated into just the one. But this gives room to grow and is
//ostensibly the better practice even at this stage.
#[derive(Serialize, Deserialize, Default)]
pub struct ViewModel {
    pub search_term: String,
    pub search_result: String,
}

//Ways in which the core can interact with the shell. Currently only using pre-made ones; would love
//to make new ones, but documentation on even the existing ones is pretty weak.
#[cfg_attr(feature = "typegen", derive(crux_core::macros::Export))]
#[derive(crux_core::macros::Effect)]
pub struct Capabilities {
    pub http: Http<Event>,
    pub key_value: KeyValue<Event>,
    pub render: Render<Event>,
}

#[derive(Default)]
pub struct Craving {}

impl App for Craving {
    //I don't know why these type declarations are a requirement for the App trait.
    //To be explict in case you are using multiple structs that fit the traits?
    type Event = Event;
    type Model = Model;
    type ViewModel = ViewModel;
    type Capabilities = Capabilities;

    //How the shell interacts with the core.
    fn update(&self, event: Event, model: &mut Model, caps: &Capabilities) {
        match event {
            //This should be a one time thing, but if views/Composables aren't handled properly,
            //it will create new core objects and call this again.
            Event::Initialize => {
                native_activity_create();
                log::info!("Starting Crux logger.");
                model.api_endpoint = String::from_utf8(Vec::from(API_IP)).unwrap().trim().to_string();
            }
            Event::SetSearch(val) => {
                model.search_term = val;
            }
            Event::Search => {
                if !model.search_term.is_empty() {
                    let mut query = model.api_endpoint.clone();
                    query.push_str("/query/");
                    query.push_str(&model.search_term);

                    caps.http.get(query).expect_json().send(Event::SetSearchResult)
                }

                caps.render.render();
            }
            Event::SetSearchResult(Ok(mut response)) => {
                log::info!("Search ok!");
                log::info!("{:?}", response.status());
                model.search_result = response.take_body();
            }
            Event::SetSearchResult(Err(e)) => {
                log::info!("Search fail");
                log::info!("{}", e);
            }
        }
    }

    fn view(&self, model: &Self::Model) -> Self::ViewModel {
        let mut search_result = String::new();
        if let Some(i) = &model.search_result {
            for item in i {
                search_result.extend(format!("{}: {:?}\n\t{}\n", item.item_name, item.ingredients, item.restaurant).chars());
            }
        }

        ViewModel {
            search_term: model.search_term.clone(),
            search_result,
        }
    }
}

//In general, testing here works great and doesn't require using an emulator/device; it can just be
//done from the command line. HOWEVER, there are some really bad limitations to testing the HTTP
//capability, which is really pretty much the only thing worth testing here.
#[cfg(test)]
mod tests {}

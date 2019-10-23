const express = require('express')
const bodyParser = require('body-parser')
const fetch = require('node-fetch')
const axios = require('axios')
const cron = require('node-cron');
const app = express()
let currency = {}
// let currency = fetch_one_hours()

//  set schedule fetch_one_hours()
let task = cron.schedule('*/10 * * * * *', () => {
  console.log('\nrunning a task every 10 seconds');
  fetch_one_hours()
});
task.start()

app.use(bodyParser.json())
app.use(bodyParser.urlencoded({
  extended: true
}))

app.get('/', (req, res) => {
  res.send('Currency Exchange Service /services/exchange to get currency exchage')
})

// get currency exchange base THB
app.get('/services/exchange', (req, res) => {
  console.log("/currency")
  res.send(currency)
})


app.listen(3000, () => {
  console.log('Start server at port 3000.')
})

// fetch data every 1 hour
async function fetch_one_hours() {
  // build api URL with user zip
  console.log("currency has been updated")
  const baseUrl = 'https://api.exchangeratesapi.io/latest?base=THB';
  await axios.get(baseUrl)
    .then((res) => currency = res.data)
}
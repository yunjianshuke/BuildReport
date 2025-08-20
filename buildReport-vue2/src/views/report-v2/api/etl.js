import { $ajax } from '@/http/index'

export function batchRunETL(data) {
    return $ajax({
        url: '/ureport/etl/batch/run',
        method: 'post',
        data
    })
}